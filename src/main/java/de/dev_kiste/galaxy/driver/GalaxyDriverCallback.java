package de.dev_kiste.galaxy.driver;

/**
 * @author Benny Lach
 */
public interface GalaxyDriverCallback<T> {
    void handleResponse(T response);
}
