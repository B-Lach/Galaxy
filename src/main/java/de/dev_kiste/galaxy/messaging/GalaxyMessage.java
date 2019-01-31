package de.dev_kiste.galaxy.messaging;

/**
 * Class representing an incoming message
 *
 * @author Benny Lach
 */
public class GalaxyMessage {
    private String payload;
    private String source;

    /**
     * Default initializer
     * @param payload message payload
     * @param source message source
     */
    public GalaxyMessage(String payload, String source) {
        this.payload = payload;
        this.source = source;
    }

    /**
     * Method to get the payload
     * @return payload
     */
    public String getPayload() {
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
