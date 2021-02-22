package Test.Benchmarks;

import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.CryptoAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.KeyAdmin;

import org.openjdk.jmh.annotations.*;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BenchmarkCrypto {

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @State(Scope.Thread)
    public static class MyState {
        public int MAC_SIG_BYTES = 6; // Estos valores son los más suceptibles a usar por el tamaño del mensaje de las Xbee
        public int IV_SIG_BYTES = 12;
        public KeyAdmin keyAdmin;
        public String plainText = "012345678901234"; // 15 Bytes
        public int CONTENT_SIG_BYTES = plainText.length();
        public CryptoAdmin encryptor;
        public CryptoAdmin decryptor;
        public byte[] encryptedText;

        public MyState() {
            try {
                keyAdmin = new KeyAdmin();
                keyAdmin.genNewIV();
                keyAdmin.genNewKey();
                encryptor = new CryptoAdmin(keyAdmin.getKey(), keyAdmin.getIV(), MAC_SIG_BYTES, IV_SIG_BYTES, CONTENT_SIG_BYTES);
                decryptor = new CryptoAdmin(keyAdmin.getKey(), keyAdmin.getIV(), MAC_SIG_BYTES, IV_SIG_BYTES, CONTENT_SIG_BYTES);
                encryptedText = encryptor.encrypt(plainText.getBytes());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // One encryption - One decryption
    // Results: Intel i59300H       avgt 1715.234 +- 20.260 ns/op
    // Results: Raspberry Pi 4b+    avgt 16323.004 +- 161.551 ns/op
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureEncryptDecrypt(MyState state) throws Exception {

        //int tests = 0;
        byte[] encryptedText;
        byte[] decryptedText;
        //while(tests < 100) { // ¿Es mejor correr 100 veces o solo una para medir ns/op?

        encryptedText = state.encryptor.encrypt(state.plainText.getBytes());
        decryptedText = state.decryptor.decrypt(encryptedText);

            //    tests++;
        // }
    }

    // Results: Intel i59300H       avgt 899.191 +- 3.859 ns/op
    // Results: Raspberry Pi 4b+    avgt 9099.071 +- 189.559 ns/op
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureEncrypt(MyState state) throws Exception {
        state.encryptor.encrypt(state.plainText.getBytes());
    }

    // Results: Intel i59300H       avgt 784.049 +- 9.107 ns/op
    // Results: Raspberry Pi 4b+    avgt 6814.786 +- 114.407 ns/op
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureDecrypt(MyState state) throws Exception {
        state.decryptor.decrypt(state.encryptedText);
    }
}
