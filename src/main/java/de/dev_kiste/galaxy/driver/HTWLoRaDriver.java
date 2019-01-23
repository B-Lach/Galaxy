package de.dev_kiste.galaxy.driver;

import com.fazecast.jSerialComm.SerialPort;

import com.sun.istack.internal.NotNull;
import de.dev_kiste.galaxy.messaging.MessageHandler;
import de.dev_kiste.galaxy.messaging.MessageLogger;
import de.dev_kiste.galaxy.security.AccessControlHandler;
import de.dev_kiste.galaxy.security.AccessStatus;
import de.dev_kiste.galaxy.security.AllowAllAccessControlHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * @author Benny Lach
 *
 * GalaxyDriver implementation for a LoRa Device
 */
public class HTWLoRaDriver implements GalaxyDriver {

    private class CallbackContainer<T> {
        private CompletableFuture<T> future;
        private Class<T> type;

        public CallbackContainer(CompletableFuture<T> callback, Class<T> type) {
            this.future = callback;
            this.type = type;
        }
    }

    private String portDescriptor;
    private String configString = "433000000,20,6,12,1,1,0,0,0,0,3000,8,4";

    volatile private ArrayList<CallbackContainer> callbackStack = new ArrayList<>();
    private SerialPort port;
    volatile private String serialResponse = "";

    volatile private MessageHandler messageHandler = new MessageLogger();
    volatile private AccessControlHandler accessControlHandler = new AllowAllAccessControlHandler();
    volatile private boolean isConnecting = false;
    volatile private boolean shouldRead = true;
    /**
     * Default initializer
     *
     * @param descriptor descriptor of the used port
     * @throws IllegalArgumentException if descriptor is null
     */
    public HTWLoRaDriver(String descriptor) throws IllegalArgumentException {
        if (descriptor == null || descriptor.isEmpty()) {
            throw new IllegalArgumentException("Descriptor must not be null nor empty");
        }
        portDescriptor = descriptor;
    }

    @Override
    public CompletableFuture<Boolean> sendMessage(String msg, String receiver) throws IllegalArgumentException {
        if(msg == null || receiver == null) {
            throw new IllegalArgumentException("Message and received must not be null!");
        }
        int length = msg.getBytes().length;

        if(!sendSerialMessage("AT+SEND=" + length, false)) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture();
        CompletableFuture<Boolean> sizeFuture = new CompletableFuture();

        sizeFuture.thenCompose((didSetSize) -> {
            if (!didSetSize || !sendSerialMessage(msg, false)) {
                return CompletableFuture.completedFuture("AT,NOSENDING");
            }
            CompletableFuture<String> sendFuture = new CompletableFuture();
            HTWLoRaDriver.this.callbackStack.add(new CallbackContainer(sendFuture, String.class));

            return sendFuture;
        }).thenCompose((startSendMessage) -> {
            if (!startSendMessage.equals("AT,SENDING")) {
                return CompletableFuture.completedFuture("AT,NOTSENDED");
            }
            CompletableFuture<String> didSendFuture = new CompletableFuture();
            callbackStack.add(new CallbackContainer(didSendFuture, String.class));

            return didSendFuture;
        }).thenApply((sendedMessage) -> {
            future.complete(sendedMessage.equals("AT,SENDED"));

            return null;
        });
        callbackStack.add(new CallbackContainer(sizeFuture, Boolean.class));

        return future;
    }

    @Override
    public CompletableFuture<Boolean> sendBroadcastMessage(String msg) throws IllegalArgumentException {
        return sendMessage(msg, "FFFF");
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        if(messageHandler != null) {
            messageHandler = handler;
        }
    }

    @Override
    public void setAccessControlHandler(AccessControlHandler handler) {
        if(handler != null) {
            accessControlHandler = handler;
        }
    }

    @Override
    public int getMaximumPayloadSize() {
        return 250;
    }

    @Override
    public CompletableFuture<String> getAddress() {
        if(!sendSerialMessage("AT+ADDR?", false)) {
            return CompletableFuture.completedFuture("");
        }

        CompletableFuture<String> future = new CompletableFuture();
        CompletableFuture<String> response = new CompletableFuture();

        response.thenCompose((result) -> {
            String[] split = result.split(",");
            if(split.length != 3 || !split[2].equals("OK")) {
                future.complete("");
            } else {
                String address = split[1];

                future.complete(address.length() == 4 ? address : "");
            }
            return null;
        });
        callbackStack.add(new CallbackContainer(response, String.class));

        return future;
    }

    @Override
    public CompletableFuture<Boolean> setAddress(String address) {
        if(address == null) {
            return CompletableFuture.completedFuture(false);
        }
        if(!sendSerialMessage("AT+ADDR=" + address, false)) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture();

        callbackStack.add(new CallbackContainer(future, Boolean.class));

        return future;
    }

    @Override
    public int[] getSupportedChannels() {
        return new int[0];
    }

    @Override
    public CompletableFuture<Integer> getChannel() {
        return CompletableFuture.completedFuture(-1);
    }

    @Override
    public CompletableFuture<Boolean> setChannel(int channel) throws IllegalArgumentException {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> connect() throws NullPointerException {
        if(port != null && port.isOpen()) {
            return CompletableFuture.completedFuture(true);
        }
        if(!setupSerialConnection()) {
            return CompletableFuture.completedFuture(false);
        }
        return setSerialConfig();
    }

    private Boolean setupSerialConnection() {
        port = SerialPort.getCommPort(portDescriptor);
        port.setComPortParameters(115200, 8,1, 0);

        return port.openPort();
    }

    private CompletableFuture<Boolean> setSerialConfig() {
        startReading();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture<Boolean> configFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> switchedModeFuture = new CompletableFuture<>();

        callbackStack.add(new CallbackContainer(configFuture, Boolean.class));
        callbackStack.add(new CallbackContainer(switchedModeFuture, Boolean.class));

        configFuture.thenCompose((didSetConfig) -> {
            if(!didSetConfig) {
                callbackStack.clear();
                isConnecting = false;

                return CompletableFuture.completedFuture(false);
            }
            sendSerialMessage("AT+RX", true);

            return switchedModeFuture;
        }).thenCompose((didSwitchMode) -> {
            isConnecting = false;
            future.complete(didSwitchMode);

            return null;
        });
        isConnecting = true;
        sendSerialMessage("AT+CFG=" + configString, true);

        return future;
    }

    private void startReading() {
        shouldRead = true;
        Thread t = new Thread(() -> {
            String message = "";
            try {
                while (HTWLoRaDriver.this.shouldRead) {
                    while (port.bytesAvailable() <= 0) {
                        Thread.sleep(20);
                    }

                    byte[] readBuffer = new byte[port.bytesAvailable()];
                    port.readBytes(readBuffer, readBuffer.length);
                    message += new String(readBuffer, StandardCharsets.UTF_8);

                    if(message.endsWith("\r\n")) {
                        String payload = message.replace("\r\n", "");
                        message = "";

                        HTWLoRaDriver.this.handleIncomingMessage(payload);
                    }
                }
            } catch (Exception e) {
                // TODO: Replace with logger
                System.out.println("Reading failed: " + e);
                message = "";
            }
        });
        t.start();
    }

    private void handleIncomingMessage(@NotNull String message) {
        if(message.startsWith("LR")) {
            handleRemoteMessage(message);
        } else if(message.startsWith("AT")){
            handleModuleMessage(message);
        } else {
            System.out.println("Unknown message received: " + message);
        }
    }

    @Override
    public CompletableFuture<Boolean> disconnect() {
        shouldRead = false;

        if(port == null || !port.isOpen()) {
            return CompletableFuture.completedFuture(true);
        }
        return CompletableFuture.completedFuture(port.closePort());
    }

    @Override
    public CompletableFuture<Boolean> reboot() {
        if(port == null || !port.isOpen() || !sendSerialMessage("AT+RST", false)) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture();

        callbackStack.add(new CallbackContainer(future, Boolean.class));

        return future;
    }

    private void handleRemoteMessage(String message) {
        // Example Payload:  LR,0000,0A,Hallo Test
        String[] header = message.substring(0, 9).split(",");
        // TODO: Ok to ignore message if header size not valid?
        if(header.length == 3) {
            String source = header[1];
            String payload = message.substring(11);

            if(isValidSource(source)) {
                messageHandler.receivedMessage(payload, header[1]);
            }
        }
    }

    private boolean isValidSource(String source) {
        return accessControlHandler.getAccessStatus(source) == AccessStatus.AUTHORIZED ||
                accessControlHandler.determineAuthorizationStatus(source) == AccessStatus.AUTHORIZED;
    }

    private void handleModuleMessage(String message) {
        if(callbackStack.size() > 0) {
            CallbackContainer container = callbackStack.remove(0);
            switch (container.type.getSimpleName()) {
                case "String":
                    container.future.complete(message);
                    break;
                case "Integer":
                    String integerString = message.replace("\nOK\n", "");

                    container.future.complete(Integer.parseInt(integerString));
                    break;
                case "Boolean":
                    container.future.complete(message.endsWith("OK"));
                    break;
                default:
                    // TODO: Use logging not System.out
                    System.out.println("Unimplemented support type: " + container.type);
            }
        } else {
            // TODO: Use logging not System.out
            System.out.println("Not able to forward message -> no handler available");
        }
    }

    private boolean sendSerialMessage(String msg, boolean isSetupMessage) throws IllegalStateException {
        if (port == null || !port.isOpen()) {
            throw new IllegalStateException("Port is not connected");
        }

        if(!isSetupMessage && isConnecting) {
            return false;
        }

        byte[] byteMsg = (msg + "\r\n").getBytes(StandardCharsets.UTF_8);
        port.writeBytes(byteMsg, byteMsg.length);

        return true;
    }
}
