package ApplicationLayer.LocalServices.WirelessService;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.CryptoAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.State;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Initializer;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Message;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WirelessReceiver extends WirelessService{
    XbeeReceiver xbeeReceiver;
    List<Service> otherServices;

    // To save memory
    char currentHeader;
    ArrayList<AppComponent> appCompUpdated;

    public WirelessReceiver(List<AppComponent> components, String XBEE_PORT, boolean encrypt, List<Service> otherServices) throws Exception {
        super(components, encrypt);
        this.appCompUpdated = new ArrayList<>();

        this.XBEE_PORT = XBEE_PORT;
        this.otherServices = otherServices;

        xbeeReceiver = new XbeeReceiver(XBEE_BAUD, XBEE_PORT);
    }

    /**
     * Testing constructor
     * @param components list of AppComponents
     * @throws Exception
     */
    public WirelessReceiver(List<AppComponent> components, boolean encrypt, List<Service> otherServices) throws Exception {
        super(components, encrypt);
        this.appCompUpdated = new ArrayList<>();

        this.otherServices = otherServices;
        xbeeReceiver = new XbeeReceiver();
    }

    /**
     * FOR TESTING: PASSING TO XBEEsENDER TO BUT BYTES DIRECLTY IN QUEUE
     * @return
     */
    public XbeeReceiver getXbeeReceiver() {
        return xbeeReceiver;
    }

    /**
     * Debe sacar los componentes pendientes de su lista, si esta vacía no entra
     */
    @Override
    public void run() {
        // 0. Dispatch xbeeReceiver thread
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(xbeeReceiver);

        // 1. Get the messages, parse them, and inform otherServices about AppComponent updates.
        while(true) {
            for (int i= 0; i < this.xbeeReceiver.sizeOfQueue(); i++) {      // Saca de una pasada tantos byte[] como habían en cola hasta evaluar la condición
                try {
                    // 0. Extract bytes from Xbee and encrypt if needed
                    byte[] bytes = this.xbeeReceiver.consumeByteFromQueue(); // Extraer bytes RAW
                    byte[] b = encrypt ? cryptoAdmin.decrypt(bytes) : bytes; // Desencriptar mensaje

                    // 1. Extract header and found upUpdated Message
                    currentHeader = (char) b[0];                   // Extraer header
                    Message m = this.map.get(currentHeader);

                    // 2. Update Message with new values
                    m.bytes = b;

                    // 3. Update all associated States, and theirs AppComponent's real values
                    appCompUpdated.clear();
                    for (State s : m.myStates) {
                        // 3.1 Update int[] myValues inside State, given the data from MessageWithIndex
                        State.MessagesWithIndexes mi = s.hashOfMyMessagesWithIndexes.get(currentHeader); // Obtengo mensaje correspondiente con indices
                        BitOperations.updateValuesFromByteArray(s.myValues, s.bitSignificativos, b, mi.myBitSig_inicio, mi.raw_inicio, mi.raw_fin); // Update de values[] míos según el mensaje que acabo de leer

                        // 3.2 Update double[] valoresReales (directo) actuales con data de int[] myValues
                        for (int j = 0; j < s.len; j++) {
                            s.myAppComponent.valoresRealesActuales[i] = (s.myValues[i] - s.offset[i]) * Math.pow(10, -s.decimales[i]);
                        }

                        // 3.3 Add to list of AppComponents updated
                        appCompUpdated.add(s.myAppComponent);
                    }

                    // 4. Inform to all services that all m.myStates's AppComponent have been updated
                    for (Service service : otherServices) {
                        service.putListOfComponentsInQueue(appCompUpdated);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


}
