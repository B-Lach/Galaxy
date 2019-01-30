package de.dev_kiste.galaxy.client;

import de.dev_kiste.galaxy.client.middleware.GalaxyMiddleware;
import de.dev_kiste.galaxy.driver.GalaxyDriver;
import de.dev_kiste.galaxy.messaging.MessageHandler;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Helper to build a GalaxyNode
 *
 * @author Benny Lach
 */
public class GalaxyNodeBuilder {
    private Optional<GalaxyDriver> driver = Optional.empty();
    private Optional<MessageHandler> messageHandler = Optional.empty();
    private ArrayList<GalaxyMiddleware> middlewares = new ArrayList();

    private boolean isDebug = false;

    /**
     * Method to set the used {@link GalaxyDriver} object
     *
     * @param driver The driver to use
     * @return Updated GalaxyNodeBuilderInterface object
     */
    public GalaxyNodeBuilder setDriver(GalaxyDriver driver) {
        this.driver = Optional.ofNullable(driver);

        return this;
    }

    /**
     * Method to set the used {@link MessageHandler} object
     *
     * @param messageHandler The used message handler object
     * @return Updated GalaxyNodeBuilderInterface object
     */
    public GalaxyNodeBuilder setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = Optional.ofNullable(messageHandler);

        return this;
    }

    public GalaxyNodeBuilder use(GalaxyMiddleware middleware) {
        Optional.ofNullable(middleware).ifPresent(value -> middlewares.add(value));

        return this;
    }

    public GalaxyNodeBuilder isDebug() {
        this.isDebug = true;

        return this;
    }

    public GalaxyNode build() {
        return new GalaxyNode(this);
    }

    /**
     * Package private Getter for the {@link GalaxyDriver} object
     *
     * @return the used driver
     */
    Optional<GalaxyDriver> getDriver() {
        return driver;
    }

    /**
     * Package private Getter for the {@link MessageHandler} object
     *
     * @return the used message handler
     */
    Optional<MessageHandler> getMessageHandler() {
        return messageHandler;
    }

    /**
     * Package private Getter of defined {@link GalaxyMiddleware} objects
     * @return defined middlewares
     */
    ArrayList<GalaxyMiddleware> getMiddlewares() { return middlewares; }

    boolean getIsDebug() {
        return isDebug;
    }
}
