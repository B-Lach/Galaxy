package de.dev_kiste.galaxy.driver;

import com.fazecast.jSerialComm.SerialPort;

import de.dev_kiste.galaxy.messaging.GalaxyMessage;
import de.dev_kiste.galaxy.messaging.MessageHandler;
import de.dev_kiste.galaxy.messaging.MessageLogger;

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

    volatile private MessageHandler messageHandler = new MessageLogger();
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
        return sendPayload(msg.getBytes(StandardCharsets.UTF_8), receiver);
    }

    @Override
    public CompletableFuture<Boolean> sendBroadcastMessage(String msg) throws IllegalArgumentException {
        return sendMessage(msg, "FFFF");
    }

    @Override
    public CompletableFuture<Boolean> sendPayload(byte[] payload, String receiver) throws IllegalArgumentException {
        if(payload == null || receiver == null) {
            throw new IllegalArgumentException("Payload and receiver must not be null!");
        }
        if(payload.length > getMaximumPayloadSize()) {
            throw new IllegalArgumentException("Payload must not exceed the maximum supported payload size!");
        }
        return setDestinationAddress(receiver)
                .thenCompose((didSet) -> {
                    if(!didSet) {
                        return CompletableFuture.completedFuture(false);
                    }
                    return _sendPayload(payload);
                });
    }

    @Override
    public CompletableFuture<Boolean> sendBroadcastPayload(byte[] payload) throws IllegalArgumentException {
        return sendPayload(payload, "FFFF");
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        if(messageHandler != null) {
            messageHandler = handler;
        }
    }

    @Override
    public int getMaximumPayloadSize() {
        return 250;
    }

    @Override
    public CompletableFuture<String> getAddress() {
        if(!sendSerialPayload("AT+ADDR?".getBytes(StandardCharsets.UTF_8), false)) {
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
        if(!sendSerialPayload(("AT+ADDR=" + address).getBytes(StandardCharsets.UTF_8), false)) {
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
            sendSerialPayload("AT+RX".getBytes(StandardCharsets.UTF_8), true);

            return switchedModeFuture;
        }).thenCompose((didSwitchMode) -> {
            isConnecting = false;
            future.complete(didSwitchMode);

            return null;
        });
        isConnecting = true;
        sendSerialPayload(("AT+CFG=" + configString).getBytes(StandardCharsets.UTF_8), true);

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

    private void handleIncomingMessage(String message) {
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
        if(port == null || !port.isOpen() || !sendSerialPayload("AT+RST".getBytes(StandardCharsets.UTF_8), false)) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture();

        callbackStack.add(new CallbackContainer(future, Boolean.class));

        return future;
    }

    private CompletableFuture<Boolean> setDestinationAddress(String address) {
        if (address == null) {
            return CompletableFuture.completedFuture(false);
        }
        if(!sendSerialPayload(("AT+DEST=" + address).getBytes(StandardCharsets.UTF_8), false)) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture();

        callbackStack.add(new CallbackContainer(future, Boolean.class));

        return future;
    }

    private CompletableFuture<Boolean> _sendPayload(byte[] payload) {
        //int length = payload.length;

        if(!sendSerialPayload(("AT+SEND=" + payload.length).getBytes(StandardCharsets.UTF_8), false)) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture();
        CompletableFuture<Boolean> sizeFuture = new CompletableFuture();

        sizeFuture.thenCompose((didSetSize) -> {
            if (!didSetSize || !sendSerialPayload(payload, false)) {
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

    private void handleRemoteMessage(String message) {
        // Example Payload:  LR,0000,0A,Hallo Test
        String[] header = message.substring(0, 9).split(",");
        // TODO: Ok to ignore message if header size not valid?
        if(header.length == 3) {
            String source = header[1];
            String payload = message.substring(11);

            messageHandler.received(new GalaxyMessage(payload, source));
        }
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

    private boolean sendSerialPayload(byte[] payload, boolean isSetupMessage) throws IllegalStateException {
        if (port == null || !port.isOpen()) {
            throw new IllegalStateException("Port is not connected");
        }

        if(!isSetupMessage && isConnecting) {
            return false;
        }
        byte[] end = "\r\n".getBytes(StandardCharsets.UTF_8);
        final byte[] bytes = new byte[payload.length + end.length];

        System.arraycopy(payload, 0, bytes, 0, payload.length);
        System.arraycopy(end, 0, bytes, payload.length, end.length);

        port.writeBytes(bytes, bytes.length);

        return true;
    }
}
