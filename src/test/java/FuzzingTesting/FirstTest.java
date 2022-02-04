package FuzzingTesting;

import org.junit.jupiter.api.Test;

public class FirstTest {

    @Test
    public void singleRunTest() {
        I2CFuzzer fuzzer = new I2CFuzzer();
        I2CRunner Main = new I2CRunner(fuzzer);
        Main.run();
        Main.runs(4);
    }
}
