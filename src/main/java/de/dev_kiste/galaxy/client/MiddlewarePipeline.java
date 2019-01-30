package de.dev_kiste.galaxy.client;

import de.dev_kiste.galaxy.client.middleware.GalaxyMiddleware;
import de.dev_kiste.galaxy.messaging.GalaxyMessage;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Class to execute a list of {@link GalaxyMiddleware} objects
 * 
 * @author Benny Lach
 */
class MiddlewarePipeline {
    private final ArrayList<GalaxyMiddleware> middlewares;
    private GalaxyMessage message;

    MiddlewarePipeline(ArrayList<GalaxyMiddleware> middlewares, GalaxyMessage message) {
        this.middlewares = middlewares;
        this.message = message;
    }

    /**
     * Method to execute all defined middleware
     * @param callback Will be called if every middleware was executed and none was canceled
     */
    void execute(Consumer<GalaxyMessage> callback) {
        _execute(callback, 0);

        // TODO: One may find a better approach to use allOf and be able to stop execution if one of the futures was canceled
        // Cancel a single future does not interrupt the chain. It just notifies the combined future at the end that one of the futures
        // was canceled but we need to stop the execution of all waiting futures. Using the recursive approach now.

//        CompletableFuture f = new CompletableFuture();
//
//        List<CompletableFuture<GalaxyMessage>> futureList = middlewares.stream().map((middleware) -> {
//            CompletableFuture<GalaxyMessage> future = new CompletableFuture();
//            middleware.execute(message, () -> future.complete(message), () -> future.cancel(true));
//
//            return future;
//        }).collect(Collectors.toList());
//
//        try {
//            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()]))
//                    .thenAccept((nothing) -> callback.accept(message))
//                    .get();
//        } catch (Exception e) {
//            System.out.println(e);
//        }


    }

    /**
     * Concrete implementation to execute all defined middleware objects
     * @param callback Callback to execute at the end of the chain
     * @param index current middleware index
     */
    private void _execute(Consumer<GalaxyMessage> callback, final int index) {
        if(middlewares.isEmpty() || index >= middlewares.size()) {
            callback.accept(message);
            return;
        }
        CompletableFuture<GalaxyMessage> f = new CompletableFuture<>();
        middlewares.get(index).execute(message, (value) -> f.complete(value), () -> f.cancel(true));

        try {
            f.thenAccept((message) -> {
                _execute(callback, index + 1);
            }).get();
        } catch (Exception e) {}
    }
}
