package de.dev_kiste.galaxy.driver;

/**
 * @author Benny Lach
 */
public interface GalaxDriverCallback<T> {
    void handleResponse(T response);
}
