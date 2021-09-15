//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WebSocketService;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import io.socket.engineio.client.transports.WebSocket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";
    public XbeeReceiver xbeeReceiver;

    public Main() {
    }

    public static AppComponent findAppComponent(List<AppComponent> list, String componentID) throws Exception {
        Iterator var2 = list.iterator();

        AppComponent a;
        do {
            if (!var2.hasNext()) {
                throw new Exception("Component with ID " + componentID + " was not found in AppComponent list.");
            }

            a = (AppComponent)var2.next();
        } while(!a.getID().equals(componentID));

        return a;
    }

    public static void main(String[] args) throws Exception {
        List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(dir);
        List<Service> ls = new ArrayList<>();
        PrintService ps = new PrintService();
        WebSocketService wss = new WebSocketService();
        ls.add(ps);
        ls.add(wss);

        TestChannel tc = new TestChannel(lac, ls);
        Thread t1 = new Thread(tc);
        Thread t2 = new Thread(ps);
        Thread t3 = new Thread(wss);
        t1.start();
        t2.start();
        t3.start();
        Thread.sleep(10000);
        

    }

    void receiverSetup() throws Exception {
//        List<AppReceiver> appReceivers = CSVToAppComponent.CSVs_to_AppReceivers(this.dir);
//        PrintService printService = new PrintService();
//        WirelessReceiver wirelessReceiver = new WirelessReceiver(appReceivers);
//        this.xbeeReceiver = wirelessReceiver.getXbeeReceiver();
//        Iterator var4 = appReceivers.iterator();
//
//        while(var4.hasNext()) {
//            AppComponent ac = (AppComponent)var4.next();
//            ac.subscribeToService(printService);
//        }
//
//        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);
//        mainExecutor.submit(wirelessReceiver.getXbeeReceiver());
//        mainExecutor.submit(wirelessReceiver.getReceiverAdmin());
//        mainExecutor.submit(printService);
//        mainExecutor.shutdown();
    }

    void senderSetup() throws Exception {
//        List<AppComponent> appSenders = CSVToAppComponent.CSVs_to_AppComponents(this.dir);
//        LinkedList<RandomReader> randomReaders = new LinkedList();
//        Iterator var3 = appSenders.iterator();
//
//        while(var3.hasNext()) {
//            AppComponent as = (AppComponent)var3.next();
//            randomReaders.add(new RandomReader(as, 1000L));
//        }
//
//        SequentialReaderExecutor sequentialReaderExecutor = new SequentialReaderExecutor();
//        Iterator var9 = randomReaders.iterator();
//
//        while(var9.hasNext()) {
//            SensorsReader sr = (SensorsReader)var9.next();
//            sequentialReaderExecutor.addReader(sr);
//        }
//
//        PrintService printService = new PrintService();
//        WirelessSender wirelessSender = new WirelessSender(appSenders, this.xbeeReceiver);
//        Iterator var6 = appSenders.iterator();
//
//        while(var6.hasNext()) {
//            AppComponent ac = (AppComponent)var6.next();
//            ac.subscribeToService(printService);
//            ac.subscribeToService(wirelessSender);
//        }
//
//        ExecutorService mainExecutor = Executors.newFixedThreadPool(4);
//        mainExecutor.submit(wirelessSender.getXbeeSender());
//        mainExecutor.submit(wirelessSender);
//        mainExecutor.submit(sequentialReaderExecutor);
//        mainExecutor.submit(printService);
//        mainExecutor.shutdown();
    }
}
