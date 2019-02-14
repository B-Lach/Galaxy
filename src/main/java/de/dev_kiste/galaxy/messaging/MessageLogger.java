package de.dev_kiste.galaxy.messaging;

import java.nio.charset.StandardCharsets;

/**
 * @author Benny Lach
 *
 * Reference Implementation of {@link MessageHandler} logging reveived messages to {@link System#out}
 *
 */
public class MessageLogger implements MessageHandler {
    @Override
    public void received(GalaxyMessage message) {
        String payloadString = new String(message.getPayload(), StandardCharsets.UTF_8);

        System.out.println("Received new message from " + message.getSource() + " :\n" + payloadString);
    }
}
