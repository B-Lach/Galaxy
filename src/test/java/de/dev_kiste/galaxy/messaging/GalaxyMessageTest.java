package de.dev_kiste.galaxy.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author Benny Lach
 */
class GalaxyMessageTest {

    @Test
    void testConstructor() {
        String payload = "foo";
        String source = "bar";

        GalaxyMessage m = new GalaxyMessage(payload, source);

        assertEquals(source, m.getSource());
        assertEquals(payload, m.getPayload());
    }
}
