package ApplicationLayer.LocalServices.WirelessService;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppReceiver;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.CryptoAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Components.State;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Components.StateReceiver;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Initializer.ReceiverInitializer;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Messages.ReceivedMessage;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.Receiving.ReceiverAdmin;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.Receiving.XbeeReceiver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WirelessReceiver extends WirelessService{
    ReceiverAdmin receiverAdmin;
    XbeeReceiver xbeeReceiver;
    CryptoAdmin cryptoAdmin;
    boolean decrpyt;
    List<Service> otherServices;

    // To save memory
    char currentHeader;
    ArrayList<AppComponent> appCompUpdated;

    public WirelessReceiver(List<AppReceiver> components, String XBEE_PORT, boolean decrpyt, List<Service> otherServices) throws Exception {
        this.appCompUpdated = new ArrayList<>();
        this.decrpyt = decrpyt;
        this.XBEE_PORT = XBEE_PORT;
        this.otherServices = otherServices;

        cryptoAdmin = setupCryptoAdmin();
        xbeeReceiver = new XbeeReceiver(XBEE_BAUD, XBEE_PORT);

        LinkedList<State> state_list = new LinkedList<>(); // Only for initializer

        for (AppReceiver c: components ) {
            StateReceiver newState = new StateReceiver(c.getID(), c.getMinimosConDecimal(), c.getMaximosConDecimal(), c);
            state_list.add(newState);
            states.put(c.getID(), newState); // Local global map
        }

        // Initializer of States/Messages
        ReceiverInitializer receiverInitializer = new ReceiverInitializer(state_list, MSG_SIZE_BITS, FIRST_HEADER);
        map = receiverInitializer.genMessages();

        // Receiver Admin
        receiverAdmin = new ReceiverAdmin(xbeeReceiver, map, cryptoAdmin);

    }

    /**
     * Testing constructor
     * @param components list of AppComponents
     * @throws Exception
     */
    public WirelessReceiver(List<AppReceiver> components, boolean decrpyt, List<Service> otherServices) throws Exception {
        this.appCompUpdated = new ArrayList<>();
        this.decrpyt = decrpyt;
        this.otherServices = otherServices;

        cryptoAdmin = setupCryptoAdmin();
        xbeeReceiver = new XbeeReceiver();

        LinkedList<State> state_list = new LinkedList<>(); // Only for initializer

        for (AppReceiver c: components ) {
            StateReceiver newState = new StateReceiver(c.getID(), c.getMinimosConDecimal(), c.getMaximosConDecimal(), c);
            state_list.add(newState);
            states.put(c.getID(), newState); // Local global map
        }

        // Initializer of States/Messages
        ReceiverInitializer receiverInitializer = new ReceiverInitializer(state_list, MSG_SIZE_BITS, FIRST_HEADER);
        map = receiverInitializer.genMessages();

        // Receiver Admin
        receiverAdmin = new ReceiverAdmin(xbeeReceiver, map, cryptoAdmin);

    }

    /**
     * FOR TESTING: PASSING TO XBEEsENDER TO BUT BYTES DIRECLTY IN QUEUE
     * @return
     */
    public XbeeReceiver getXbeeReceiver() {
        return xbeeReceiver;
    }


    public ReceiverAdmin getReceiverAdmin() {
        return receiverAdmin;
    }


    /**
     * Debe sacar los componentes pendientes de su lista, si esta vacía no entra
     */
    @Override
    public void run() {
        // Dispatch Xbee & xbeeReceiver threads
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(xbeeReceiver);

        while(true) {
            for (int i= 0; i < this.xbeeReceiver.sizeOfQueue(); i++) {      // Saca de una pasada tantos byte[] como habían en cola hasta evaluar la condición

                // 0. Extract bytes from Xbee and decrpyt if needed
                byte[] bytes = this.xbeeReceiver.consumeByteFromQueue();        // Extraer bytes RAW
                byte[] b = bytes;

                if (decrpyt) {
                    try {
                        b = this.cryptoAdmin.decrypt(bytes);    // Desencriptar mensaje
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 1. Extract header and found upUpdated Message
                currentHeader = (char) b[0];                   // Extraer header
                ReceivedMessage m = (ReceivedMessage) this.map.get(currentHeader);

                // 2. Update Message with new values
                m.bytes = b;

                // 3. Update all associated States, and theirs AppComponent's real values
                appCompUpdated.clear();
                for (StateReceiver s: m.myStates) {
                    // 3.1 Update int[] myValues inside State, given the data from MessageWithIndex
                    State.MessagesWithIndexes mi = s.hashOfMyMessagesWithIndexes.get(currentHeader); // Obtengo mensaje correspondiente con indices
                    BitOperations.updateValuesFromByteArray(s.myValues, s.bitSignificativos, b, mi.myBitSig_inicio, mi.raw_inicio, mi.raw_fin); // Update de values[] míos según el mensaje que acabo de leer

                    // 3.2 Update double[] valoresReales (directo) actuales con data de int[] myValues
                    for (int j = 0; j < s.len; j++) {
                        s.myAppReceiver.valoresRealesActuales[i] = (s.myValues[i] - s.offset[i]) * Math.pow(10, - s.decimales[i]);
                    }

                    // 3.3 Add to list of AppComponents updated
                    appCompUpdated.add(s.myAppReceiver);
                }

                // 4. Inform to all services that all m.myStates's AppComponent have been updated
                for (Service service: otherServices) {
                    service.putListOfComponentsInQueue(appCompUpdated);
                }
            }
        }
    }


}
