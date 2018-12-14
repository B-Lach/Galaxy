package de.dev_kiste.galaxy.security;

/**
 * @author Benny Lach
 *
 * Access Control Handler Interface
 */
public interface AccessControlHandler {

    /**
     * Method to request the current Access Status for the given identifier
     *
     * @param identifier The identifier of the client - typically it's MAC address
     * @return current {@link AccessStatus} value
     */
    AccessStatus getAccessStatus(String identifier);

    /**
     * Method to determine the authorization status for a given client.
     * One must call this method if {@link AccessControlHandler#getAccessStatus(String)}
     * returns {@link AccessStatus#NOT_DETERMINED}
     *
     * @param identifier The identifier of the client - typically it's MAC address
     * @return calculated {@link AccessStatus} for the client
     */
    AccessStatus determineAuthorizationStatus(String identifier);
}
