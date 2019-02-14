package de.dev_kiste.galaxy.messaging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author Benny Lach
 */
class GalaxyMessageTest {

    @Test
    void testConstructor() {
        byte[] payload = "foo".getBytes(StandardCharsets.UTF_8);
        String source = "bar";

        GalaxyMessage m = new GalaxyMessage(payload, source);

        assertEquals(source, m.getSource());
        // payloads must not share the same pointer ...
        assertNotEquals(payload, m.getPayload());
        // .. but the content must be the same
        assertArrayEquals(payload, m.getPayload());
    }
}
