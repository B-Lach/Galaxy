package de.dev_kiste.galaxy.client.middleware;

/**
 * MiddlewareStopper interface used for stopping middleware execution
 *
 * @author Benny Lach
 */
public interface MiddlewareStopper {
    /**
     * Triggers stop of further execution
     */
    void stop();
}
