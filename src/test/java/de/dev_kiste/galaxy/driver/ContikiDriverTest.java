package de.dev_kiste.galaxy.driver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * @author Benny Lach
 *
 * Unit tests for ContikiDriver
 */
public class ContikiDriverTest {
    private String portDescriptor = "cu.usbmodem14201";
    private ContikiDriver testDriver;

    @Before
    public void setup() {
        testDriver = new ContikiDriver(portDescriptor);
    }

    @After
    public void deinit() {
        testDriver.disconnect();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initWithNullPortDescriptorShouldFail() {
        ContikiDriver driver = new ContikiDriver(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initWitEmptyPortDescriptorShouldFail() {
        ContikiDriver driver = new ContikiDriver("");
    }

    @Test
    public void initWitNonEmptyPortDescriptorShouldNotFail() {
        try {
            ContikiDriver driver = new ContikiDriver(portDescriptor);
        } catch (Exception e){
            Assert.fail();
        }
    }

    @Test
    public void connectWithInvalidDescriptorShouldReturnFalse() {
        ContikiDriver driver = new ContikiDriver("foo");
        Assert.assertFalse(driver.connect());
    }

    @Test
    public void connectWithValidDescriptorShouldReturnTrue() {
        Assert.assertTrue(testDriver.connect());
    }

    @Test
    public void connectUsedPortShouldReturnFalse() {
        Assert.assertTrue(testDriver.connect());

        ContikiDriver driver2 = new ContikiDriver(portDescriptor);
        Assert.assertFalse(driver2.connect());
    }

    @Test
    public void disconnectNotOpenedPortShouldReturnFalse() {
        Assert.assertFalse(testDriver.disconnect());
    }

    @Test
    public void disconnectOpenedPortShouldReturnTrue() {
        testDriver.connect();
        Assert.assertTrue(testDriver.disconnect());
    }


    @Test
    public void testSupportedChannels() {
        int[] expectedChannels = IntStream.rangeClosed(11, 26).toArray();

        ContikiDriver driver = new ContikiDriver(portDescriptor);
        int[] supportedChannels = driver.getSupportedChannels();

        Assert.assertTrue(expectedChannels.length == supportedChannels.length);

        for(int channel: supportedChannels) {
            IntStream channels = IntStream.of(expectedChannels);
            Assert.assertTrue(channels.anyMatch(x -> x == channel) == true);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void setChannelWhileNotConnectedShouldFail() {
        testDriver.setChannel(20, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNegativeChannelShouldFail() {
        testDriver.connect();
        testDriver.setChannel(-10, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnsupportedChannelShouldFail() {
        testDriver.connect();
        testDriver.setChannel(40, null);

    }

    @Test
    public void setSupportedChannelShouldNotFail() {
        testDriver.connect();
        testDriver.setChannel(15, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAddressWithNullCallbackShouldFail() {
        testDriver.connect();
        testDriver.getAddress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getChannelWithNullCallbackShouldFail() {
        testDriver.connect();
        testDriver.getChannel(null);
    }

    @Test(expected = IllegalStateException.class)
    public void sendMessageWithoutConnectShouldFail() {
        testDriver.sendMessage("Ping", "FFFF", null);
    }

    @Test
    public void sendMessageWithoutCallbackShouldNotCrash() {
        testDriver.connect();
        testDriver.sendMessage("Ping", "FFFF", null);
    }

    @Test
    public void doubleConnectShouldReturnTrue() {
        Assert.assertTrue(testDriver.connect());
        Assert.assertTrue(testDriver.connect());
    }

    @Test
    public void rebootNotConnectedPortShouldReturnFalse() {
        Assert.assertFalse(testDriver.reboot());
    }

    @Test
    public void rebootConnectedPortShouldReturnTrue() {
        Assert.assertTrue(testDriver.connect());
        Assert.assertTrue(testDriver.reboot());
    }
}
