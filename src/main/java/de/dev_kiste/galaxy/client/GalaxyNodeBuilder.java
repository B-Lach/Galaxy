package de.dev_kiste.galaxy.client;

import de.dev_kiste.galaxy.client.middleware.GalaxyMiddleware;
import de.dev_kiste.galaxy.driver.GalaxyDriver;
import de.dev_kiste.galaxy.messaging.MessageHandler;
import de.dev_kiste.galaxy.security.AccessControlHandler;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Helper to build a GalaxyNode
 *
 * @author Benny Lach
 */
public class GalaxyNodeBuilder {
    private Optional<GalaxyDriver> driver;
    private Optional<MessageHandler> messageHandler;
    private Optional<AccessControlHandler> accessControlHandler;
    private ArrayList<GalaxyMiddleware> middlewares = new ArrayList();

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
     * Method th set the used {@link AccessControlHandler} object
     *
     * @param accessControlHandler The used access handler implementation
     * @return Updated GalaxyNodeBuilderInterface object
     */
    public GalaxyNodeBuilder setAccessControlHandler(AccessControlHandler accessControlHandler) {
        this.accessControlHandler = Optional.ofNullable(accessControlHandler);

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
     * Package private Getter for the {@link AccessControlHandler} object
     *
     * @return the used access handler
     */
    Optional<AccessControlHandler> getAccessControlHandler() {
        return accessControlHandler;
    }

    /**
     * Package private Getter of defined {@link GalaxyMiddleware} objects
     * @return defined middlewares
     */
    ArrayList<GalaxyMiddleware> getMiddlewares() { return middlewares; }
}
