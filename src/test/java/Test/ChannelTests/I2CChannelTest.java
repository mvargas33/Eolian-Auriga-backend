package Test.ChannelTests;

import ApplicationLayer.Channel.I2C;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class I2CChannelTest {

    public void connectionTest() {
        I2C i2cChannel = new I2C(new ArrayList<>(), new ArrayList<>());

        ExecutorService mainExecutor = Executors.newFixedThreadPool(1);
        mainExecutor.submit(i2cChannel);
        mainExecutor.shutdown();
    }

    public static void main(String[] args) {
        I2CChannelTest test = new I2CChannelTest();
        try{
            test.connectionTest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
