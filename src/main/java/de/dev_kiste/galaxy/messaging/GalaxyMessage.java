package de.dev_kiste.galaxy.messaging;

/**
 * Class representing an incoming message
 *
 * @author Benny Lach
 */
public class GalaxyMessage {
    private byte[] payload;
    private String source;

    /**
     * Default initializer
     * @param payload message payload
     * @param source message source
     */
    public GalaxyMessage(byte[] payload, String source) {
        this.source = source;
        this.payload = payload.clone();
    }

    /**
     * Method to get the payload
     * @return payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Method to get the source
     * @return the source
     */
    public String getSource() {
        return source;
    }
}
