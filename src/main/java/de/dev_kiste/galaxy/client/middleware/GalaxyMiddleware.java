package de.dev_kiste.galaxy.client.middleware;

import de.dev_kiste.galaxy.messaging.GalaxyMessage;

/**
 * GalaxyMiddleware Interface
 *
 * @author Benny Lach
 */
public interface GalaxyMiddleware {
    interface GalaxyCaller {
        void call();
    }

    /**
     * Method that holds the middleware logic which will be executed
     *
     * @param message The received message
     * @param next Callback which must be called at the end to go to the next stage
     * @param cancel Callback which must be called to stop the execution
     */
    void execute(GalaxyMessage message, GalaxyCaller next, GalaxyCaller cancel);
}
