package de.dev_kiste.galaxy.security;

/**
 * @author Benny Lach
 *
 * Example Implementation of AccessControlHandler to allow every client on the same channel
 */
public class AllowAllAccessControlHandler implements AccessControlHandler {
    @Override
    public AccessStatus getAccessStatus(String identifier) {
        return identifier == null ? AccessStatus.NOT_VALID : AccessStatus.AUTHORIZED;
    }

    @Override
    public AccessStatus determineAuthorizationStatus(String identifier) {
        return identifier == null ? AccessStatus.NOT_VALID : AccessStatus.AUTHORIZED;
    }
}
