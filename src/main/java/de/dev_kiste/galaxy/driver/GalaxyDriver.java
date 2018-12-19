package de.dev_kiste.galaxy.driver;

import de.dev_kiste.galaxy.messaging.MessageHandler;
import de.dev_kiste.galaxy.security.AccessControlHandler;

/**
 * @author Benny Lach
 *
 * The Interface a Driver implementation must implement to configure and interact with the underlying hardware module
 */
public interface GalaxyDriver {
    /**
     * Method to send a message to a given receiver
     *
     * @param msg The message to send
     * @param receiver The receiver - typically the MAC address
     */
    void sendMessage(String msg, String receiver);

    /**
     * Method to broadcast a specific message to all connected clients
     *
     * @param msg The message to send
     */
    void sendBroadcastMessage(String msg);

    /**
     * Method to set the handler for incoming messages
     *
     * If a message was received, the Driver implementation must call {@link MessageHandler#receivedMessage(String, String)}
     * to consume the incoming message
     *
     * @param handler The Handler to use
     */
    void setMessageHandler(MessageHandler handler);

    /**
     * Method to set the Access Control Handler
     *
     * If a new connection was requested or a new message was received one must ask the Access Control Handler first before
     * delegating the request to the next stage.
     *
     * @param handler The handler to use
     */
    void setAccessControlHandler(AccessControlHandler handler);

    /**
     * Method to get the address used by the underlying hardware module
     *
     * @return Address as String
     */
    String getAddress();

    /**
     * Method to get all supported Channels for the driver
     * @return Supported channels
     */
    int[] getSupportedChannels();

    /**
     * Method to get the currently used channel
     *
     * @return The used channel
     */
    int getChannel();

    /**
     * Method to set the used channel of the underlying hardware module
     *
     * @param channel The channel to use
     * @return Boolean indicating if the channel was set successfully
     * @throws IllegalArgumentException if the committed channel is not valid
     */
    boolean setChannel(int channel) throws IllegalArgumentException;

    /**
     * Method to trigger a reboot of the underlying hardware module
     *
     * @return Boolean indicating if the reboot was made
     */
    boolean reboot();
}
