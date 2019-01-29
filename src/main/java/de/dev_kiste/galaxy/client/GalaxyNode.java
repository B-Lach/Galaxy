package de.dev_kiste.galaxy.client;

import de.dev_kiste.galaxy.client.middleware.GalaxyMiddleware;
import de.dev_kiste.galaxy.driver.GalaxyDriver;
import de.dev_kiste.galaxy.messaging.GalaxyMessage;
import de.dev_kiste.galaxy.messaging.MessageHandler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Galaxy Node
 *
 * @author Benny Lach
 */
public class GalaxyNode {
    private Optional<GalaxyDriver> driver;
    private Optional<MessageHandler> messageHandler;
    private ArrayList<GalaxyMiddleware> middlewares;

    private String address;
    private boolean didBootstrap = false;

    // TODO: Remove AccessControlHandler from Driver -> it should not care about
    GalaxyNode(GalaxyNodeBuilder builder) {

        driver = builder.getDriver();
        messageHandler = builder.getMessageHandler();
        middlewares = builder.getMiddlewares();
    }

    /**
     * Method to bootstrap all needed subsystems required to work properly
     *
     * @return Future indicating if bootstrappin succeeded
     *
     */
    public CompletableFuture<Boolean> bootstrap() {
        return driver.map(driver -> {
            driver.setMessageHandler(message -> proceedIncomingMessage(message));

            return driver.connect()
                    .thenCompose((didConnect) -> {
                        didBootstrap  = didConnect;

                        return CompletableFuture.completedFuture(didConnect);
                    });
        }).orElse(CompletableFuture.completedFuture(false));
    }

    /**
     * Method to disconnect from the underlying hardware module
     *
     * @return Future indicating if module was disconnected.
     */
    public CompletableFuture<Boolean> disconnect() {

        return driver.map(GalaxyDriver::disconnect)
                .map(future -> future.thenCompose(disconnected -> {
                    didBootstrap = false;
                    address = null;

                    return CompletableFuture.completedFuture(disconnected);
                }))
                .orElse(CompletableFuture.completedFuture(false));
    }

    /**
     * Method to set the used node address
     *
     * @param newAddress The address to set
     * @return Future indicating if the new address was set.
     * @throws NullPointerException if there is no
     */
    public CompletableFuture<Boolean> setAddress(String newAddress) {
        return driver.map(driver -> driver.setAddress(newAddress))
                .map(future -> future.thenCompose(didSet -> {
                    if(didSet) {
                        this.address = newAddress;
                    }
                    return CompletableFuture.completedFuture(didSet);
                }))
                .orElse(CompletableFuture.completedFuture(false));
    }

    /**
     * Method to get the currently used address of the node
     *
     * @return Future containing the current address
     */
    public CompletableFuture<String> getAddress() {

        if(address != null) {
            return CompletableFuture.completedFuture(address);
        }

        return driver.map(GalaxyDriver::getAddress)
                .map(future -> future.thenCompose(address -> {
                    this.address = address;

                    return CompletableFuture.completedFuture(address);
                }))
                .orElse(CompletableFuture.completedFuture(""));
    }

    /**
     * Returns an array of all supported channels of the underlying hardware module.
     * - The implementation is hardware specific and maybe there is no channel supported at all.
     *
     * @return Futrue containing all supported channels
     */
    public CompletableFuture<int[]> getSupportedChannels() {
        return driver.map(GalaxyDriver::getSupportedChannels)
                .map(CompletableFuture::completedFuture)
                .orElse(CompletableFuture.completedFuture(new int[]{}));
    }

    /**
     * Returns the currently used channel of the underlying hardware module.
     * - The implementation is hardware specific and maybe there is no channel information available at all.
     *
     * @return Future containing currently used channel
     */
    public CompletableFuture<Integer> getChannel() {
        return driver.map(GalaxyDriver::getChannel)
                .orElse(CompletableFuture.completedFuture(-1));
    }

    /**
     * Updates the used channel of the underlying hardware module.
     * - The implementation is hardware specific and it may be not possible to change the channel
     * @param channel The channel to use
     *
     * @return Future indicating if the channel was set
     */
    public CompletableFuture<Boolean> setChannel(final int channel) {
        return driver.map(driver -> driver.setChannel(channel))
                .orElse(CompletableFuture.completedFuture(false));
    }

    /**
     * Method to send a message to a specific host
     * - The Node must be bootstrapped {@link GalaxyNode#bootstrap} before sending is working
     *
     * @param msg The payload to send
     * @param receiver The destination address
     * @return Future indicating if the message was send
     */
    public CompletableFuture<Boolean> sendMessage(final String msg, final String receiver) {
        if(!didBootstrap) {
            return CompletableFuture.completedFuture(false);
        }
        // TODO: Compare message length with max payload size and split into multiple messages if needed
        return driver.map(driver -> driver.sendMessage(msg, receiver))
                .orElse(CompletableFuture.completedFuture(false));
    }

    /**
     * Method to send a broadcast message
     * - The Node must be bootstrapped {@link GalaxyNode#bootstrap} before sending is working
     *
     * @param msg The payload to send
     * @return Future indicating if the message was send
     */
    public CompletableFuture<Boolean> sendBroadcastMessage(final String msg) {
        if(!didBootstrap) {
            return CompletableFuture.completedFuture(false);
        }
        // TODO: Compare message length with max payload size and split into multiple messages if needed
        return driver.map(driver -> driver.sendBroadcastMessage(msg))
                .orElse(CompletableFuture.completedFuture(false));
    }

    private void proceedIncomingMessage(GalaxyMessage message) {
        CompletableFuture.supplyAsync(() -> {
            MiddlewarePipeline pipe = new MiddlewarePipeline(middlewares, message);

            pipe.execute((result) -> {
                messageHandler.map((handler) -> {
                    handler.received(result);
                    return null;
                });

            });
            return null;
        });
    }
}
