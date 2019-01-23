package de.dev_kiste.galaxy;

import de.dev_kiste.galaxy.driver.GalaxyDriver;
import de.dev_kiste.galaxy.driver.HTWLoRaDriver;

import java.util.concurrent.CompletableFuture;

/**
 * Hello world!
 *
 */
public class App
{
    private volatile static GalaxyDriver driver;

    public static void main( String[] args ) {
        lora();
    }

    private static void lora() {
        try {
            driver = new HTWLoRaDriver("cu.SLAB_USBtoUART");

            driver.connect().thenCompose((connected) -> {
                if(connected) {
                    System.out.println("Is connected");

                    return driver.setAddress("001F");
                }
                System.out.println("Failed to connect");

                return CompletableFuture.completedFuture(false);
            }).thenCompose((changedAddr) -> {
                System.out.println("Changed address: " + changedAddr);

                return driver.sendBroadcastMessage("This is Major Tom for Ground Control");
            }).thenCompose((msgSend) -> {
                System.out.println("Did send message: " + msgSend);

                return driver.disconnect();
            }).thenCompose((disconnected) -> {
                System.out.println("Disconnected: " + disconnected);

                return null;
            });
        } catch (Exception e) {
            System.out.println("Failed with exception: " + e);
        }
    }
}
