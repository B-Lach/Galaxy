package de.dev_kiste.galaxy.messaging;

/**
 * @author Benny Lach
 */
public class GalaxyMessage {
    private String payload;
    private String source;

    public GalaxyMessage(String payload, String source) {
        this.payload = payload;
        this.source = source;
    }

    public String getPayload() {
        return payload;
    }

    public String getSource() {
        return source;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
