package ApplicationLayer.LocalServices.WirelessService;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.CryptoAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Components.State;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Components.StateSender;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Initializer.SenderInitializer;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.Receiving.XbeeReceiver;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.Sending.XbeeSender;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WirelessSender extends WirelessService{
    StateSender actState; // To save memory
    ArrayList<byte []> bytesToSendNow;

    XbeeSender xbeeSender;
    CryptoAdmin cryptoAdmin;
    boolean encrypt;

    public WirelessSender(List<AppSender> components, String XBEE_PORT, boolean encrypt) throws Exception{
        this.bytesToSendNow = new ArrayList<>();
        this.XBEE_PORT = XBEE_PORT;
        this.encrypt = encrypt;
        cryptoAdmin = setupCryptoAdmin();
        xbeeSender = new XbeeSender(XBEE_BAUD, XBEE_PORT, (int) MSG_RAW_SIZE_BYTES);

        LinkedList<State> state_list = new LinkedList<>(); // Only for initializer

        for (AppSender c: components ) {
            StateSender newState = new StateSender(c.getID(), c.getMinimosConDecimal(), c.getMaximosConDecimal(), senderAdmin);
            state_list.add(newState);
            states.put(c.getID(), newState); // Local global map
        }

        // Initializer of States/Messages
        SenderInitializer senderInitializer = new SenderInitializer(state_list,MSG_SIZE_BITS, FIRST_HEADER);
        map = senderInitializer.genMessages();
    }

    /**
     * Testing Constructor
     * @param components List of AppComponents
     * @param xbeeReceiver Offline testing xbeeReceiver object
     * @throws Exception
     */
    public WirelessSender(List<AppSender> components, XbeeReceiver xbeeReceiver, boolean encrypt) throws Exception{
        this.bytesToSendNow = new ArrayList<>();
        this.encrypt = encrypt;
        cryptoAdmin = setupCryptoAdmin();
        xbeeSender = new XbeeSender(xbeeReceiver);

        LinkedList<State> state_list = new LinkedList<>(); // Only for initializer

        for (AppSender c: components ) {
            StateSender newState = new StateSender(c.getID(), c.getMinimosConDecimal(), c.getMaximosConDecimal(), senderAdmin);
            state_list.add(newState);
            states.put(c.getID(), newState); // Local global map
        }

        // Initializer of States/Messages
        SenderInitializer senderInitializer = new SenderInitializer(state_list,MSG_SIZE_BITS, FIRST_HEADER);
        map = senderInitializer.genMessages();
    }

    @Override
    protected void serve(AppComponent c) {
        // 1. Get 1:1 associated StateSender, that holds info about wich Messages this Component is associated to
        actState = (StateSender) this.states.get(c.getID());

        // 2. Update StateSender's int[] myValues, using just updated double[] valoresRealesActuales
        for (int i = 0; i < actState.len; i++) {
            actState.myValues[i] = (int) Math.floor( c.valoresRealesActuales[i] * Math.pow(10, actState.decimales[i]) ) + actState.offset[i];
        }

        // 3. For Message that is associated with StateSender, update bytes in corresponding range
        bytesToSendNow.clear(); // Clear previous list

        for (State.MessagesWithIndexes m : actState.listOfMyMessagesWithIndexes
        ) {
            BitOperations.updateByteArrayFromValues(actState.myValues, actState.bitSignificativos, m.message.bytes, m.myBitSig_inicio, m.raw_inicio, m.raw_fin);
            // Add to batch bytes to send
            if(encrypt){
                bytesToSendNow.add(cryptoAdmin.encrypt(m.message.bytes)); // Encrypt if needed
            }else{
                bytesToSendNow.add(m.message.bytes);
            }
        }

        // 4. Send Array<bytes[]> through Xbee
        this.xbeeSender.sendBatch(bytesToSendNow);
    }

}
