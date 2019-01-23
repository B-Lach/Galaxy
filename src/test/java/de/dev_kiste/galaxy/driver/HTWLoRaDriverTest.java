package de.dev_kiste.galaxy.driver;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Benny Lach
 *
 * Unit tests for HTWLoRaDriver
 */
public class HTWLoRaDriverTest {
    private String portDescriptor = "cu.SLAB_USBtoUART";
    private HTWLoRaDriver testDriver;

    @BeforeEach
    public void setup() {
        testDriver = new HTWLoRaDriver(portDescriptor);
    }

    @AfterEach
    public void deinit() {
        testDriver.disconnect();
    }

    @Test
    public void initWithNullPortDescriptorShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new HTWLoRaDriver(null));
    }

    @Test
    public void initWitEmptyPortDescriptorShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new HTWLoRaDriver(""));

    }

    @Test
    public void initWitNonEmptyPortDescriptorShouldNotFail() {
        assertAll(() -> new HTWLoRaDriver(portDescriptor));

    }

    @Test
    public void connectWithInvalidDescriptorShouldReturnFalse() {
        HTWLoRaDriver driver = new HTWLoRaDriver("foo");

        assertAll(() -> assertFalse(driver.connect().get()));
    }

    @Test
    public void connectWithValidDescriptorShouldReturnTrue() {
        assertAll(() -> assertTrue(testDriver.connect().get()));
    }

    @Test
    public void connectUsedPortShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> {
                    HTWLoRaDriver driver2 = new HTWLoRaDriver(portDescriptor);
                    assertFalse(driver2.connect().get());
                });

    }

    @Test
    public void disconnectNotOpenedPortShouldReturnTrue() {
        assertAll(() -> assertTrue(testDriver.disconnect().get()));
    }

    @Test
    public void disconnectOpenedPortShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.disconnect().get())
        );
    }


    @Test
    public void testSupportedChannels() {
        HTWLoRaDriver driver = new HTWLoRaDriver(portDescriptor);
        int[] supportedChannels = driver.getSupportedChannels();

        assertTrue(0 == supportedChannels.length);
    }

    @Test
    public void setChannelWhileNotConnectedShouldFail() {
        assertAll(() -> assertFalse(testDriver.setChannel(20).get()));

    }

    @Test
    public void setNegativeChannelShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertFalse(testDriver.setChannel(-10).get())
        );
    }

    @Test
    public void setPositiveChannelShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertFalse(testDriver.setChannel(10).get())
        );
    }

    @Test
    public void setNullAddressShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertFalse(testDriver.setAddress(null).get())
        );
    }

    @Test
    public void setValidAddressShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.setAddress("0010").get()),
                () -> assertEquals("0010", testDriver.getAddress().get())
        );
    }

    @Test
    public void sendMessageWithNullPayloadShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> assertFalse(testDriver.sendMessage(null, "FFFF").get()));
    }

    @Test
    public void sendMessageWithNullReceiverShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> assertFalse(testDriver.sendMessage("Foo", null).get()));
    }

    @Test
    public void sendMessageWithoutConnectShouldThrow() {
        assertThrows(IllegalStateException.class, () -> assertFalse(testDriver.sendMessage("Ping", "FFFF").get()));
    }

    @Test
    public void sendValidMessageShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.sendMessage("Foo", "0010").get())
        );
    }

    @Test
    public void sendBroadcastWithNullPayloadShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> assertFalse(testDriver.sendBroadcastMessage(null).get()));
    }

    @Test
    public void sendBroadcastWithoutConnectShouldThrow() {
        assertThrows(IllegalStateException.class, () -> assertFalse(testDriver.sendBroadcastMessage("Ping").get()));
    }

    @Test
    public void sendValidBroadcastShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.sendBroadcastMessage("Foo").get())
        );
    }

    @Test
    public void doubleConnectShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.connect().get())
        );
    }

    @Test
    public void rebootNotConnectedPortShouldReturnFalse() {
        assertAll(() -> assertFalse(testDriver.reboot().get()));
    }

    @Test
    public void rebootConnectedPortShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.reboot().get())
        );
    }
}
