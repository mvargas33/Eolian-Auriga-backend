package Test.UnitTests;

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
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureEncryptDecrypt(MyState state) throws Exception {
        KeyAdmin keyAdmin = new KeyAdmin();
        keyAdmin.genNewIV();
        keyAdmin.genNewKey();
        String plainText = "012345678901234"; // 15 Bytes;
        int CONTENT_SIG_BYTES = plainText.length();

        CryptoAdmin encryptor = new CryptoAdmin(keyAdmin.getKey(), keyAdmin.getIV(), state.MAC_SIG_BYTES, state.IV_SIG_BYTES, CONTENT_SIG_BYTES);
        CryptoAdmin decryptor = new CryptoAdmin(keyAdmin.getKey(), keyAdmin.getIV(), state.MAC_SIG_BYTES, state.IV_SIG_BYTES, CONTENT_SIG_BYTES);

        int tests = 0;
        byte[] encryptedText;
        byte[] decryptedText;
        while(tests < 100) {
            encryptedText = encryptor.encrypt(plainText.getBytes());
            //assertEquals((MAC_SIG_BYTES + IV_SIG_BYTES) % 16, (encryptedText.length % 16));
            decryptedText = decryptor.decrypt(encryptedText);
            //assertArrayEquals(plainText.getBytes(), decryptedText);
            tests++;
        }

    }
}
