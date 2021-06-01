package Test.XbeeTest;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.HexUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@EnabledIf("true") //cambiar a true cuando haya una xbee conectada, podria cambiarse a algo como "OS==Raspbian"
public class ConnectionTests {
    int BAUD_RATE;
    String PORT_RECEIVE;
    String PORT_SEND;
    String DATA_TO_SEND;
    byte[] DATA_TO_SEND_BYTES;
    String REMOTE_NODE_IDENTIFIER;
//    XBeeDevice myDeviceR;
//    XBeeDevice myDeviceS;
//    RemoteXBeeDevice myRemoteDevice;
//    XBeeDevice myDeviceRS;

    /**
     * Handler genérico para Xbee Receiver a usar en todos los tests
     */
    class testReceiveListener implements IDataReceiveListener {
        @Override
        public void dataReceived(XBeeMessage xbeeMessage) {
            System.out.format("From %s >> %s | %s%n", xbeeMessage.getDevice().get64BitAddress(),
                    HexUtils.prettyHexString(HexUtils.byteArrayToHexString(xbeeMessage.getData())),
                    new String(xbeeMessage.getData()));
        }
    }

    /**
     * Setup de parámetros de los tests. Baudrate, nombre de los puertos de las Xbee.
     * Handler genérico del Xbee Receiver
     */
    @BeforeEach
    public void xbeeSetup(){
        BAUD_RATE = 9600;
        PORT_RECEIVE = "COM5";
        PORT_SEND = "COM4"; ///dev/ttyUSB0
        DATA_TO_SEND = "Hola! Probando ...";
        DATA_TO_SEND_BYTES = DATA_TO_SEND.getBytes();
        REMOTE_NODE_IDENTIFIER = "RECEIVER_1";
    }


    public XBeeDevice xbeeReceiverSetup(){
        XBeeDevice myDeviceR = new XBeeDevice(PORT_RECEIVE, BAUD_RATE);
        try {
            myDeviceR.open();
            myDeviceR.addDataListener(new testReceiveListener());
            System.out.println("\n>> Xbee Receiver: Waiting for data...");
            return myDeviceR;
        } catch (XBeeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public XBeeDevice xbeeSenderSetup(){
        XBeeDevice sender = new XBeeDevice(PORT_SEND, BAUD_RATE);
        try {
            sender.open();
            System.out.println("\n>> Xbee Sender: Ready...");
            return sender;
        } catch (XBeeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public RemoteXBeeDevice xbeeRemoteSetup(XBeeDevice myDeviceS){
        // Obtain the remote XBee device from the XBee network.
        XBeeNetwork xbeeNetwork = myDeviceS.getNetwork();
        RemoteXBeeDevice myRemoteDevice = null;
        try {
            myRemoteDevice = xbeeNetwork.discoverDevice(REMOTE_NODE_IDENTIFIER);
            return myRemoteDevice;
        } catch (XBeeException e) {
            e.printStackTrace();
        }
        System.out.println("Couldn't find the remote XBee device with '" + REMOTE_NODE_IDENTIFIER + "' Node Identifier.");
        System.exit(1);
        return myRemoteDevice;
    }

    public void sendMessageWithTarget(XBeeDevice sender, RemoteXBeeDevice remote){
        try {
            sender.sendData(remote, DATA_TO_SEND_BYTES);
            System.out.println("XbeeSender Success: Mensaje enviado a Xbee Recevier");
        } catch (XBeeException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageBroadcast(XBeeDevice sender){
        try {
            sender.sendBroadcastData(DATA_TO_SEND_BYTES);
            System.out.println("XbeeSender Success: Mensaje enviado como broadcast");
        } catch (XBeeException e) {
            e.printStackTrace();
        }
    }


    /**
     * Prueba iniciar un dispositivo enviador que hace broadcast de mensajes
     */
    @Test
    public void senderBroadcastTest() {
        XBeeDevice sender = xbeeSenderSetup();
        XBeeDevice receiver = xbeeReceiverSetup();

        // Enviar 10 smensajes y parar
        for (int i = 0; i < 10; i++) {
            try {
                sendMessageBroadcast(sender);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
    }

    @Test
    public void senderTargetTest(){
        XBeeDevice sender = xbeeSenderSetup();
        RemoteXBeeDevice remote = xbeeRemoteSetup(sender);

        System.out.format("Sending data to device %s ...", remote.get64BitAddress());

        // Enviar 10 smensajes y parar
        for(int i = 0; i < 10; i++) {
            try {
                sendMessageWithTarget(sender, remote);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * Configura una Xbee como Receiver y como Sender.
//     * La idea es que una Xbee externa envíe datos a esta Xbee,
//     * y que esta Xbee mande mensajes a una Xbee externa,
//     * en forma de Broadcast
//     */
//    @Test
//    public void sendBroadcastAndReceiveTest(){
//        // Enviar 10 smensajes y parar
//        for(int i = 0; i < 10; i++) {
//            try {
//                sendMessageBroadcast();
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }


}
