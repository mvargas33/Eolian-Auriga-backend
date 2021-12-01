package FuzzingTesting;

import java.util.Arrays;
import java.util.Random;

public class I2CFuzzer {
    
    public byte[] fuzz() {
        byte[] data = new byte[8];
        new Random().nextBytes(data);
        return data;
    }

    public void run() {

    }

    public void runs() {

    }
    
}
