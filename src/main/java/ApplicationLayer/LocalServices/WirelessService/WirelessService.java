package ApplicationLayer.LocalServices.WirelessService;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.CryptoAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.KeyAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Initializer;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.State;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Message;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WirelessService extends Service {
    HashMap<String, State> states;
    HashMap<Character, Message> map;
    CryptoAdmin cryptoAdmin;
    boolean encrypt;

    // Cryptography common parameters
    int MAC_SIG_BYTES = 6; // Estos valores son los más suceptibles a usar por el tamaño del mensaje de las Xbee
    int IV_SIG_BYTES = 12;
    int CONTENT_SIG_BYTES = 16*5 + 15; // 5 bloques de 16 bytes, + 1 bloque de 15 bytes + 1 bloque de 16 bytes (MAC+IV)

    double MSG_RAW_SIZE_BYTES = CONTENT_SIG_BYTES + 1 + IV_SIG_BYTES + MAC_SIG_BYTES; // Tamaño de cada mensaje. Se usa para estimar delays lectura

    // Xbee network parameters
    int XBEE_BAUD = 230400;
    double XBEE_MAX_BYTES_PER_SEC_LIMIT = 7200; // Sólo puede enviar 7200 bytes por segundo A ESTE BAUDRATE y tamaño de mensaje de 114 bytes. Dato empírico
    double XBEE_MAX_MSG_PER_SEC_LIMIT = (XBEE_MAX_BYTES_PER_SEC_LIMIT/MSG_RAW_SIZE_BYTES) ;       // Mensajes por segundo (63.1) que puede enviar la Xbee a ese baudrate y tamaño de mensaje. (-1) Para holgura
    double XBEE_MAX_MSG_PERIOD_MS = ((1.0/XBEE_MAX_MSG_PER_SEC_LIMIT) * 1000) + 1;  // Delay en MS del Xbee (16ms). (+1) para holgura
    String XBEE_PORT;

    // Protocol paramseters
    int MSG_SIZE_BITS = 8*(16*5);
    int FIRST_HEADER = 56;

    public WirelessService(List<AppComponent> components, boolean encrypt) throws Exception{
        this.encrypt = encrypt;
        this.states = new HashMap<>();
        // CryptoAdmin
        cryptoAdmin = setupCryptoAdmin();

        LinkedList<State> state_list = new LinkedList<>(); // Only for initializer

        for (AppComponent c: components ) {
            State newState = new State(c);
            state_list.add(newState);
            states.put(c.getID(), newState); // Local global map
        }

        // Initializer of States/Messages
        Initializer initializer = new Initializer(state_list ,MSG_SIZE_BITS, FIRST_HEADER);
        map = initializer.genMessages();
    }

    /**
     * Ejecutarlo para cambiar las keys de sender/receiver
     * @throws NoSuchAlgorithmException Error de AES (no saldrá nunca)
     */
    void genNewKeyAndIV() throws NoSuchAlgorithmException {
        KeyAdmin keyAdmin = new KeyAdmin();
        keyAdmin.genNewKey();
        System.out.println("Key: " + keyAdmin.getKeyAsEncodedString());
        keyAdmin.genNewIV();
        System.out.println("IV : " + keyAdmin.getIVAsEncodedString());
    }

    CryptoAdmin setupCryptoAdmin() throws Exception {
        String encryptionKey = "uBb2BqdBtfJYyqOh5BmBX+HlqPGLz8/wdiXRgg8WnMs=";
        String IV = "eoCvPqhwOTO6FvGXGGPyhw==";
        KeyAdmin keyAdmin = new KeyAdmin();
        keyAdmin.setKeyFromEncodedString(encryptionKey);
        keyAdmin.setIVFromEncodedString(IV);

        return new CryptoAdmin(keyAdmin.getKey(), keyAdmin.getIV(), MAC_SIG_BYTES, IV_SIG_BYTES, CONTENT_SIG_BYTES);
    }

    @Override
    protected void serve(AppComponent c) {}

}
