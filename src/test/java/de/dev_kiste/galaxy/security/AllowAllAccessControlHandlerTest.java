package de.dev_kiste.galaxy.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benny Lach
 *
 * Unit tests for AllowAllAccessControlHandler
 */
public class AllowAllAccessControlHandlerTest {
    private AllowAllAccessControlHandler handler = new AllowAllAccessControlHandler();

    @Test
    public void getAccessStatusShouldReturnAuthorizedForNonNullString() {
        assertEquals(AccessStatus.AUTHORIZED, handler.getAccessStatus("foo"));
    }

    @Test
    public void getAccessStatusShouldReturnNotValidForNullString() {
        assertEquals(AccessStatus.NOT_VALID, handler.getAccessStatus(null));
    }

    @Test
    public void determineAuthStatusShouldReturnAuthorizedForNonNullString() {
        assertEquals(AccessStatus.AUTHORIZED, handler.determineAuthorizationStatus("bar"));
    }

    @Test
    public void determineAuthStatusShouldReturnNotValidForNullString() {
        assertEquals(AccessStatus.NOT_VALID, handler.determineAuthorizationStatus(null));
    }
}
