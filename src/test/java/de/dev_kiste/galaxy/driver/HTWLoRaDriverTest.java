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
 class HTWLoRaDriverTest {
    private String portDescriptor = "cu.SLAB_USBtoUART";
    private HTWLoRaDriver testDriver;

    @BeforeEach
    void setup() {
        testDriver = new HTWLoRaDriver(portDescriptor);
    }

    @AfterEach
    void deinit() {
        testDriver.disconnect();
    }

    @Test
    void initWithNullPortDescriptorShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new HTWLoRaDriver(null));
    }

    @Test
    void initWitEmptyPortDescriptorShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new HTWLoRaDriver(""));

    }

    @Test
    void initWitNonEmptyPortDescriptorShouldNotFail() {
        assertAll(() -> new HTWLoRaDriver(portDescriptor));

    }

    @Test
    void connectWithInvalidDescriptorShouldReturnFalse() {
        HTWLoRaDriver driver = new HTWLoRaDriver("foo");

        assertAll(() -> assertFalse(driver.connect().get()));
    }

    @Test
    void connectWithValidDescriptorShouldReturnTrue() {
        assertAll(() -> assertTrue(testDriver.connect().get()));
    }

    @Test
    void connectUsedPortShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> {
                    HTWLoRaDriver driver2 = new HTWLoRaDriver(portDescriptor);
                    assertFalse(driver2.connect().get());
                });

    }

    @Test
    void disconnectNotOpenedPortShouldReturnTrue() {
        assertAll(() -> assertTrue(testDriver.disconnect().get()));
    }

    @Test
   void disconnectOpenedPortShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.disconnect().get())
        );
    }


    @Test
    void testSupportedChannels() {
        HTWLoRaDriver driver = new HTWLoRaDriver(portDescriptor);
        int[] supportedChannels = driver.getSupportedChannels();

        assertEquals(0, supportedChannels.length);
    }

    @Test
    void setChannelWhileNotConnectedShouldFail() {
        assertAll(() -> assertFalse(testDriver.setChannel(20).get()));

    }

    @Test
    void setNegativeChannelShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertFalse(testDriver.setChannel(-10).get())
        );
    }

    @Test
    void setPositiveChannelShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertFalse(testDriver.setChannel(10).get())
        );
    }

    @Test
    void setNullAddressShouldReturnFalse() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertFalse(testDriver.setAddress(null).get())
        );
    }

    @Test
    void setValidAddressShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.setAddress("0010").get()),
                () -> assertEquals("0010", testDriver.getAddress().get())
        );
    }

    @Test
    void sendMessageWithNullPayloadShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> assertFalse(testDriver.sendMessage(null, "FFFF").get()));
    }

    @Test
    void sendMessageWithNullReceiverShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> assertFalse(testDriver.sendMessage("Foo", null).get()));
    }

    @Test
    void sendMessageWithoutConnectShouldThrow() {
        assertThrows(IllegalStateException.class, () -> assertFalse(testDriver.sendMessage("Ping", "FFFF").get()));
    }

    @Test
    void sendValidMessageShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.sendMessage("Foo", "0010").get())
        );
    }

    @Test
    void sendBroadcastWithNullPayloadShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> assertFalse(testDriver.sendBroadcastMessage(null).get()));
    }

    @Test
    void sendBroadcastWithoutConnectShouldThrow() {
        assertThrows(IllegalStateException.class, () -> assertFalse(testDriver.sendBroadcastMessage("Ping").get()));
    }

    @Test
    void sendValidBroadcastShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.sendBroadcastMessage("Foo").get())
        );
    }

    @Test
    void doubleConnectShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.connect().get())
        );
    }

    @Test
    void rebootNotConnectedPortShouldReturnFalse() {
        assertAll(() -> assertFalse(testDriver.reboot().get()));
    }

    @Test
    void rebootConnectedPortShouldReturnTrue() {
        assertAll(
                () -> assertTrue(testDriver.connect().get()),
                () -> assertTrue(testDriver.reboot().get())
        );
    }
}
