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
     * @param callback The CallbackHandler triggered with a boolean to indicate if the message was sent
     * @throws IllegalArgumentException if the message or the receiver is null
     */
    void sendMessage(String msg, String receiver, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException;

    /**
     * Method to broadcast a specific message to all connected clients
     *
     * @param msg The message to send
     * @param callback The CallbackHandler triggered with a boolean to indicate if the message was sent
     * @throws IllegalArgumentException if the message is null
     */
    void sendBroadcastMessage(String msg, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException;

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
     * Method to get the maximum size in bytes a single payload can have
     *
     * @return payload size in bytes
     */
    int getMaximumPayloadSize();
    /**
     * Method to get the address used by the underlying hardware module
     *
     * @param callback The CallbackHandler with the received address from the module
     */
    void getAddress(GalaxyDriverCallback<String> callback);

    /**
     * Method to set the address the module should use
     *
     * @param address The address to use
     * @param callback The CallbackHandler with a boolean to indicate if the address was set
     */
    void setAddress(String address, GalaxyDriverCallback<Boolean> callback);
    /**
     * Method to get all supported Channels for the driver
     *
     * @return Supported channels
     */
    int[] getSupportedChannels();

    /**
     * Method to get the currently used channel
     *
     * @param callback The CallbackHandler with the currently used channel
     */
    void getChannel(GalaxyDriverCallback<Integer> callback);

    /**
     * Method to set the used channel of the underlying hardware module
     *
     * @param channel The channel to use
     * @param callback The CallbackHandler with a boolean to indicate if the channel was set
     * @throws IllegalArgumentException if the committed channel is not valid
     */
    void setChannel(int channel, GalaxyDriverCallback<Boolean> callback) throws IllegalArgumentException;

    /**
     * Method to bootstrap and trying to connect to the underlying module
     *
     * @param callback The CallbackHandler with a boolean to indicate if the the connection was established
     */
    void connect(GalaxyDriverCallback<Boolean> callback);

    /**
     * Method to disconnect from the underlying module
     *
     * @return Boolean indicating if the connection was closed successfully
     */
    boolean disconnect();

    /**
     * Method to trigger a reboot of the underlying hardware module
     *
     * @return Boolean indicating if the reboot has been made
     */
    boolean reboot();
}
