package de.dev_kiste.galaxy.driver;

import de.dev_kiste.galaxy.messaging.MessageHandler;

import java.util.concurrent.CompletableFuture;

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
     * @throws IllegalArgumentException if the message or the receiver is null
     *
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> sendMessage(String msg, String receiver) throws IllegalArgumentException;

    /**
     * Method to broadcast a specific message to all connected clients
     *
     * @param msg The message to send
     * @throws IllegalArgumentException if the message is null
     *
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> sendBroadcastMessage(String msg) throws IllegalArgumentException;

    /**
     * Method to set the handler for incoming messages
     *
     * If a message was received, the Driver implementation must call {@link MessageHandler#received(de.dev_kiste.galaxy.messaging.GalaxyMessage)}
     * to consume the incoming message
     *
     * @param handler The Handler to use
     */
    void setMessageHandler(MessageHandler handler);

    /**
     * Method to get the maximum size in bytes a single payload can have
     *
     * @return payload size in bytes
     */
    int getMaximumPayloadSize();
    /**
     * Method to get the address used by the underlying hardware module
     *
     * @return CompletableFuture
     */
    CompletableFuture<String> getAddress();

    /**
     * Method to set the address the module should use
     *
     * @param address The address to use
     *
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> setAddress(String address);

    /**
     * Method to get all supported Channels for the driver
     *
     * @return Supported channels
     */
    int[] getSupportedChannels();

    /**
     * Method to get the currently used channel
     *
     * @return CompletableFuture
     */
    CompletableFuture<Integer> getChannel();

    /**
     * Method to set the used channel of the underlying hardware module
     *
     * @param channel The channel to use
     *
     * @return CompletableFuture
     * @throws IllegalArgumentException if the committed channel is not valid
     */
    CompletableFuture<Boolean> setChannel(int channel) throws IllegalArgumentException;

    /**
     * Method to bootstrap and trying to connect to the underlying module
     *
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> connect();

    /**
     * Method to disconnect from the underlying module
     *
     * @return CompletableFuture indicating if the connection was closed successfully
     */
    CompletableFuture<Boolean> disconnect();

    /**
     * Method to trigger a reboot of the underlying hardware module
     *
     * @return CompletableFuture indicating if the reboot has been made
     */
    CompletableFuture<Boolean> reboot();
}
