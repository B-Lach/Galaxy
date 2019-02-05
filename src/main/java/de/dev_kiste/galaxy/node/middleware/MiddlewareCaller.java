package de.dev_kiste.galaxy.node.middleware;

import de.dev_kiste.galaxy.messaging.GalaxyMessage;

/**
 * MiddlewareCaller interface used for processing middleware
 *
 * @author Benny Lach
 */
public interface MiddlewareCaller {
    /**
     * Method to trigger middleware execution has finished
     * @param message the updated message object
     */
    void call(GalaxyMessage message);
}
