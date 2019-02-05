package de.dev_kiste.galaxy.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author Benny Lach
 */
public class GalaxyMessageTest {

    @Test
    public void testConstructor() {
        String payload = "foo";
        String source = "bar";

        GalaxyMessage m = new GalaxyMessage(payload, source);

        assertEquals(source, m.getSource());
        assertEquals(payload, m.getPayload());
    }
}
