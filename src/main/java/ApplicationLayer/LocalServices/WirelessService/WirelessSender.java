package ApplicationLayer.LocalServices.WirelessService;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Encryption.CryptoAdmin;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.State;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.Initializer;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeSender;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WirelessSender extends WirelessService{
    State actState; // To save memory
    ArrayList<byte []> bytesToSendNow;

    XbeeSender xbeeSender;

    public WirelessSender(List<AppComponent> components, String XBEE_PORT, boolean encrypt) throws Exception{
        super(components, encrypt);
        this.bytesToSendNow = new ArrayList<>();
        this.XBEE_PORT = XBEE_PORT;

        xbeeSender = new XbeeSender(XBEE_BAUD, XBEE_PORT, (int) MSG_RAW_SIZE_BYTES);
    }

    /**
     * Testing Constructor
     * @param components List of AppComponents
     * @param xbeeReceiver Offline testing xbeeReceiver object
     * @throws Exception
     */
    public WirelessSender(List<AppComponent> components, XbeeReceiver xbeeReceiver, boolean encrypt) throws Exception{
        super(components, encrypt);
        this.bytesToSendNow = new ArrayList<>();

        xbeeSender = new XbeeSender(xbeeReceiver);
    }

    @Override
    protected void serve(AppComponent c) {
        // 1. Get 1:1 associated State, that holds info about wich Messages this Component is associated to
        actState = this.states.get(c.getID());

        // 2. Update State's int[] myValues, using just updated double[] valoresRealesActuales
        for (int i = 0; i < actState.len; i++) {
            actState.myValues[i] = (int) Math.floor( c.valoresRealesActuales[i] * Math.pow(10, actState.decimales[i]) ) + actState.offset[i];
        }

        // 3. For Message that is associated with State, update bytes in corresponding range
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
