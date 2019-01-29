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
     * @param payload The payload received
     * @param source The sender of the message - typically the MAC address
     */
    void received(String payload, String source);
}
