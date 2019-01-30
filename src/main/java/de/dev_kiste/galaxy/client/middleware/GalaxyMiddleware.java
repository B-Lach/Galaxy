package de.dev_kiste.galaxy.client.middleware;

import de.dev_kiste.galaxy.messaging.GalaxyMessage;

/**
 * GalaxyMiddleware Interface
 *
 * @author Benny Lach
 */
public interface GalaxyMiddleware {

    /**
     * Method that holds the middleware logic which will be executed
     *
     * @param message The received message
     * @param caller Callback which must be called at the end to go to the next stage
     * @param stopper Callback which must be called to stop the execution
     */
    void execute(GalaxyMessage message, MiddlewareCaller caller, MiddlewareStopper stopper);
}
