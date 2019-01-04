package de.dev_kiste.galaxy.driver;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import de.dev_kiste.galaxy.messaging.MessageHandler;
import de.dev_kiste.galaxy.security.AccessControlHandler;

import java.nio.charset.StandardCharsets;

/**
 * @author Benny Lach
 */
public class LoRaDriver implements GalaxyDriver, SerialPortDataListener {
    private SerialPort port;

    private String portDescriptor;
    private String serialResponse = "";

    /**
     * Default initializer
     *
     * @param descriptor descriptor of the used port
     * @throws IllegalArgumentException if descriptor is null
     */
    public LoRaDriver(String descriptor) throws IllegalArgumentException {
        if (descriptor == null || descriptor == "") {
            throw new IllegalArgumentException("Descriptor must not be null or empty");
        }
        portDescriptor = descriptor;
    }

    @Override
    public void sendMessage(String msg, String receiver, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException {

    }

    @Override
    public void sendBroadcastMessage(String msg, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException {

    }

    @Override
    public void setMessageHandler(MessageHandler handler) {

    }

    @Override
    public void setAccessControlHandler(AccessControlHandler handler) {

    }

    @Override
    public void getAddress(GalaxyDriverCallback<String> callback) {

    }

    @Override
    public void setAddress(String address, GalaxyDriverCallback<Boolean> callback) {

    }

    @Override
    public int[] getSupportedChannels() {
        return new int[0];
    }

    @Override
    public void getChannel(GalaxyDriverCallback<Integer> callback) {

    }

    @Override
    public void setChannel(int channel, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException {

    }

    @Override
    public boolean connect() {
        if(port != null && port.isOpen()) {
            return true;
        }
        port = SerialPort.getCommPort(portDescriptor);
        port.setBaudRate(115200);
//        port.setComPortParameters(115200, 8,1, 0);
        port.addDataListener(this);

        return port.openPort();
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean reboot() {
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
    }
}
