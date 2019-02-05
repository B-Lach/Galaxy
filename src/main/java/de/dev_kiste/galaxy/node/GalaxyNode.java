package de.dev_kiste.galaxy.node;

import de.dev_kiste.galaxy.node.middleware.GalaxyMiddleware;
import de.dev_kiste.galaxy.driver.GalaxyDriver;
import de.dev_kiste.galaxy.messaging.GalaxyMessage;
import de.dev_kiste.galaxy.messaging.MessageHandler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Galaxy Node
 *
 * @author Benny Lach
 */
public class GalaxyNode {
    private Optional<GalaxyDriver> driver;
    private Optional<MessageHandler> messageHandler;
    private ArrayList<GalaxyMiddleware> middlewares;

    private final Optional<Logger> logger;

    private String address;
    private boolean didBootstrap = false;

    GalaxyNode(GalaxyNodeBuilder builder) {
        driver = builder.getDriver();
        messageHandler = builder.getMessageHandler();
        middlewares = builder.getMiddlewares();
        logger = Optional.ofNullable(
                builder.getIsDebug() ?
                        Logger.getLogger("Galaxy.GalaxyNode") :
                        null
        );

        logIfNeeded(Level.INFO,
                "Node initialized\n" +
                        "Driver: " + driver + "\n" +
                        "Message Handler: " + messageHandler + "\n" +
                        "Number of middleware: " + middlewares.size());
    }

    /**
     * Method to bootstrap all needed subsystems required to work properly
     *
     * @return Future indicating if bootstrappin succeeded
     */
    public CompletableFuture<Boolean> bootstrap() {

         return driver.map(driver -> {
            driver.setMessageHandler(message -> proceedIncomingMessage(message));

            return driver.connect()
                    .thenCompose(didConnect -> {
                        didBootstrap  = didConnect;
                        logIfNeeded(Level.INFO, "Galaxy Node bootstrapping finished - Did connect: " + didConnect);

                        return CompletableFuture.completedFuture(didConnect);
                    });
        }).orElseGet( () -> {
            logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

            return CompletableFuture.completedFuture(false);
        });
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
                    logIfNeeded(Level.INFO, "Disconnected from underlying hardware module: " + disconnected);

                    return CompletableFuture.completedFuture(disconnected);
                }))
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(false);
                });
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
                    logIfNeeded(Level.INFO, "Did updated address: " + didSet);
                    if(didSet) {
                        this.address = newAddress;
                    }
                    return CompletableFuture.completedFuture(didSet);
                }))
                .orElseGet( () -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(false);
                });
    }

    /**
     * Method to get the currently used address of the node
     *
     * @return Future containing the current address
     */
    public CompletableFuture<String> getAddress() {

        if(address != null) {
            logIfNeeded(Level.INFO, "Will return cached address");
            return CompletableFuture.completedFuture(address);
        }

        return driver.map(GalaxyDriver::getAddress)
                .map(future -> future.thenCompose(address -> {
                    logIfNeeded(Level.INFO, "Requested current used address from underlying module");
                    this.address = address;

                    return CompletableFuture.completedFuture(address);
                }))
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture("");
                });
    }

    /**
     * Returns an array of all supported channels of the underlying hardware module.
     * - The implementation is hardware specific and maybe there is no channel supported at all.
     *
     * @return Futrue containing all supported channels
     */
    public CompletableFuture<int[]> getSupportedChannels() {
        return driver.map(GalaxyDriver::getSupportedChannels)
                .map(channels -> {
                    logIfNeeded(Level.INFO, "Requested supported channels fron underlying module");

                    return CompletableFuture.completedFuture(channels);
                })
                .orElseGet( () -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(new int[]{});
                });
    }

    /**
     * Returns the currently used channel of the underlying hardware module.
     * - The implementation is hardware specific and maybe there is no channel information available at all.
     *
     * @return Future containing currently used channel
     */
    public CompletableFuture<Integer> getChannel() {
        return driver.map(GalaxyDriver::getChannel)
                .map(future -> future.thenCompose(channel -> {
                    logIfNeeded(Level.INFO, "Requested currently used channel");

                    return CompletableFuture.completedFuture(channel);
                }))
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(-1);
                });
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
                .map(future -> future.thenCompose(didSetChannel -> {
                    logIfNeeded(Level.INFO, "Requesting channel update returned - new channel set: " + didSetChannel);

                    return CompletableFuture.completedFuture(didSetChannel);
                }))
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(false);
                });
    }

    /**
     * Returns the limit a message to be send must not exceed
     *
     * @return max. message length in bytes
     */
    public int getMaximumMessageSize() {
        return driver.map(driver -> {
            logIfNeeded(Level.INFO, "Requested maximum allowed message size" );

            return driver.getMaximumPayloadSize();
        })
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");
                    return -1;
                });
    }
    /**
     * Method to send a message to a specific host
     * - The Node must be bootstrapped {@link GalaxyNode#bootstrap} before sending is working
     * - The size of the message must not exceed the maximum allowed payload size
     *
     * @param msg The payload to send
     * @param receiver The destination address
     * @return Future indicating if the message was send
     */
    public CompletableFuture<Boolean> sendMessage(final String msg, final String receiver) {
        if(!didBootstrap) {
            logIfNeeded(Level.WARNING, "GalaxyNode has not been bootstrapped - sending messages not working");

            return CompletableFuture.completedFuture(false);
        }
        return driver.map(driver -> driver.sendMessage(msg, receiver))
                .map(future -> future.thenCompose(didSend -> {
                    logIfNeeded(Level.INFO, "Request to send message returned - did send: " + didSend);

                    return CompletableFuture.completedFuture(didSend);
                }))
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(false);
                });
    }

    /**
     * Method to send a broadcast message
     * - The Node must be bootstrapped {@link GalaxyNode#bootstrap} before sending is working
     * - The size of the message must not exceed the maximum allowed payload size
     *
     * @param msg The payload to send
     * @return Future indicating if the message was send
     */
    public CompletableFuture<Boolean> sendBroadcastMessage(final String msg) {
        if(!didBootstrap) {
            logIfNeeded(Level.WARNING, "GalaxyNode has not been bootstrapped  - sending messages not working");

            return CompletableFuture.completedFuture(false);
        }
        return driver.map(driver -> driver.sendBroadcastMessage(msg))
                .map(future -> future.thenCompose(didSend -> {
                    logIfNeeded(Level.INFO, "Request to send broadcast returned - did send: " + didSend);

                    return CompletableFuture.completedFuture(didSend);
                }))
                .orElseGet(() -> {
                    logIfNeeded(Level.WARNING, "GalaxyDriver instance is missing");

                    return CompletableFuture.completedFuture(false);
                });
    }

    private void proceedIncomingMessage(GalaxyMessage message) {
        CompletableFuture.supplyAsync(() -> {
            MiddlewarePipeline pipe = new MiddlewarePipeline(middlewares, message);
            logIfNeeded(Level.INFO, "New message received - registered middleware will be executed");

            pipe.execute((result) -> {
                logIfNeeded(Level.INFO, "Registered middleware was executed");

                messageHandler.map((handler) -> {
                    logIfNeeded(Level.INFO, "New message will be forwarded to registered message handler");

                    handler.received(result);
                    return Optional.empty();
                }).orElseGet(() -> {
                    logIfNeeded(Level.INFO, "Can not forward message because no message handler was registered");

                    return null;
                });
            });
            return null;
        });
    }

    /**
     * Method to log for debugging
     *
     * @param level The log level of the message
     * @param message The message to log
     */
    private void logIfNeeded(Level level, String message) {
        logger.ifPresent(logger -> {
            logger.log(level, message);
        });
    }
}
