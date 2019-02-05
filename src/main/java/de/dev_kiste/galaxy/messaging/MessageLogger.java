package de.dev_kiste.galaxy.messaging;

/**
 * @author Benny Lach
 *
 * Reference Implementation of {@link MessageHandler} logging reveived messages to {@link System#out}
 *
 */
public class MessageLogger implements MessageHandler {
    @Override
    public void received(GalaxyMessage message) {
        System.out.println("Received new message from " + message.getSource() + " :\n" + message.getPayload());
    }
}
