package de.dev_kiste.galaxy.messaging;

/**
 * @author Benny Lach
 *
 * Message Handler Interface
 */
public interface MessageHandler {
    /**
     * Method to notify about incoming messages
     *
     * @param msg The message received
     * @param identifier The sender of the message - typically the MAC address
     */
    void receivedMessage(String msg, String identifier);
}
