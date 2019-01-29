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
     * @param message The received message object
     */
    void received(GalaxyMessage message);
}
