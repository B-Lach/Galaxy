package de.dev_kiste.galaxy.driver;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import de.dev_kiste.galaxy.messaging.MessageHandler;
import de.dev_kiste.galaxy.security.AccessControlHandler;
import de.dev_kiste.galaxy.security.AllowAllAccessControlHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * @author Benny Lach
 *
 * An implementation of GalaxyDriver of the Contiki OS for the TI CC2531 Module with the following custom
 * Firmware: https://github.com/Steven1811/Texas-Instruments-CC2531-AT-Command-Firmware
 */
public class ContikiDriver implements GalaxyDriver, SerialPortDataListener {

    private class CallbackContainer<T> {
        private GalaxyDriverCallback<T> callback;
        private Class<T> type;

        public CallbackContainer(GalaxyDriverCallback<T> callback, Class<T> type) {
            this.callback = callback;
            this.type = type;
        }
    }

    private ArrayList<CallbackContainer> callbackStack = new ArrayList<>();
    private AccessControlHandler accessHandler = new AllowAllAccessControlHandler();
    private MessageHandler messageHandler;
    private SerialPort port;

    private String portDescriptor;
    private String serialResponse = "";

    private int[] allowedChannels = IntStream.rangeClosed(11, 26).toArray();

    /**
     * Default initializer
     *
     * @param descriptor descriptor of the used port
     * @throws IllegalArgumentException if descriptor is null
     */
    public ContikiDriver(String descriptor) throws IllegalArgumentException {
        if (descriptor == null || descriptor == "") {
            throw new IllegalArgumentException("Descriptor must not be null or empty");
        }
        portDescriptor = descriptor;
    }


    @Override
    public void sendMessage(String msg, String receiver, GalaxyDriverCallback<Boolean> callback) {
        // TODO: Port must be specified
        boolean send = sendSerialMessage("AT+SEND " + receiver + " 8765 " + msg);

        if(callback != null) {
            if(send == true) {
                callbackStack.add(new CallbackContainer(callback, Boolean.class));
            } else {
                callback.handleResponse(false);
            }
        }
    }

    @Override
    public void sendBroadcastMessage(String msg, GalaxyDriverCallback<Boolean> callback) {
        // TODO: Broadcasting is not possible with IPv6 - only multicast can be used - does it work with Contiki and which
        //  Multicast address do we have to use?
        if(callback != null) {
            callback.handleResponse(false);
        }
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        messageHandler = handler;
    }

    @Override
    public void setAccessControlHandler(AccessControlHandler handler) {
        if (handler != null) {
            accessHandler = handler;
        }
    }

    @Override
    public void getAddress(GalaxyDriverCallback<String> callback) throws IllegalArgumentException {
        if(callback == null) {
            throw new IllegalArgumentException("Callback must not be null!");
        }

        if(sendSerialMessage("AT+LOCIP") == true) {
            callbackStack.add(new CallbackContainer(callback, String.class));
        }
    }

    @Override
    public int[] getSupportedChannels() {
        return allowedChannels;
    }

    @Override
    public void getChannel(GalaxyDriverCallback<Integer> callback) throws IllegalArgumentException {
        if(callback == null) {
            throw new IllegalArgumentException("Callback must not be null!");
        }

        if(sendSerialMessage("AT+CH") == true) {
            callbackStack.add(new CallbackContainer(callback, Integer.class));
        }
    }

    @Override
    public void setChannel(int channel, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException {
        if(channel < 0) {
            throw new IllegalArgumentException("Channel must not be negative");
        }
        if (IntStream.of(allowedChannels).anyMatch(x -> x == channel) == false ) {
            throw new IllegalArgumentException("Channel is not supported");
        }

        boolean send = sendSerialMessage("AT+CH " + channel);
        if(send == true && callback != null) {
            callbackStack.add(new CallbackContainer(callback, Boolean.class));
        }

    }

    @Override
    public boolean connect() {
        if(port != null && port.isOpen()) {
            return true;
        }
        port = SerialPort.getCommPort(portDescriptor);
        port.setComPortParameters(115200, 8,1, 0);
        port.addDataListener(this);

        return port.openPort();
    }

    @Override
    public boolean disconnect() {
        if (port != null && port.isOpen()) {
            return port.closePort();
        }
        return false;
    }

    @Override
    public boolean reboot() {
        if(port == null || port.isOpen() == false) {
            return false;
        }
        if(sendSerialMessage("AT+RST")) {
            return disconnect();
        }
        return false;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE || port.bytesAvailable() < 0) {
            return;
        }
        byte[] newData = new byte[port.bytesAvailable()];
        int numRead = port.readBytes(newData, newData.length);
        String message = new String(newData, StandardCharsets.UTF_8);

        System.out.println("Read " + numRead + " bytes.");
        System.out.println("Received message: " + message);

        serialResponse += message;

        // End of serial message
        if (serialResponse.endsWith("\nOK\n") || serialResponse.endsWith("\nFAIL\n")) {
            String response = serialResponse;
            serialResponse = "";

            if(callbackStack.size() > 0) {
                CallbackContainer container = callbackStack.remove(0);

                switch (container.type.getSimpleName()) {
                    case "String":
                        String resultString = response.replace("\nOK\n", "");
                        container.callback.handleResponse(resultString);
                        break;
                    case "Integer":
                        String integerString = response.replace("\nOK\n", "");
                        container.callback.handleResponse(Integer.parseInt(integerString));
                        break;
                    case "Boolean":
                        Boolean success = response.endsWith("\nOK\n");
                        container.callback.handleResponse(success);
                        break;
                    default:
                        // TODO: Use logging not System.out
                        System.out.println("Unsupported type: " + container.type);
                }

            }
        }
    }

    private boolean sendSerialMessage(String msg) throws IllegalStateException {
        if (port == null || port.isOpen() == false) {
            throw new IllegalStateException("Port is not connected");
        }
        byte[] byteMsg = (msg + "\r\n").getBytes(StandardCharsets.UTF_8);
        port.writeBytes(byteMsg, byteMsg.length);

        return true;
    }
}