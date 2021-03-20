//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppReceiver;
import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.Receiving.XbeeReceiver;
import ApplicationLayer.SensorReading.SensorsReader;
import ApplicationLayer.SensorReading.SequentialReaderExecutor;
import ApplicationLayer.SensorReading.RandomReaders.RandomReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public String dir = "/Github/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";
    public XbeeReceiver xbeeReceiver;

    public Main() {
    }

    public static AppSender findAppSender(List<AppSender> list, String componentID) throws Exception {
        Iterator var2 = list.iterator();

        AppSender a;
        do {
            if (!var2.hasNext()) {
                throw new Exception("Component with ID " + componentID + " was not found in AppSender list.");
            }

            a = (AppSender)var2.next();
        } while(!a.getID().equals(componentID));

        return a;
    }

    public static void main(String[] args) throws Exception {
        Main main_program = new Main();
        main_program.receiverSetup();
        main_program.senderSetup();
    }

    void receiverSetup() throws Exception {
        List<AppReceiver> appReceivers = CSVToAppComponent.CSVs_to_AppReceivers(this.dir);
        PrintService printService = new PrintService();
        WirelessReceiver wirelessReceiver = new WirelessReceiver(appReceivers);
        this.xbeeReceiver = wirelessReceiver.getXbeeReceiver();
        Iterator var4 = appReceivers.iterator();

        while(var4.hasNext()) {
            AppComponent ac = (AppComponent)var4.next();
            ac.subscribeToService(printService);
        }

        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);
        mainExecutor.submit(wirelessReceiver.getXbeeReceiver());
        mainExecutor.submit(wirelessReceiver.getReceiverAdmin());
        mainExecutor.submit(printService);
        mainExecutor.shutdown();
    }

    void senderSetup() throws Exception {
        List<AppSender> appSenders = CSVToAppComponent.CSVs_to_AppSenders(this.dir);
        LinkedList<RandomReader> randomReaders = new LinkedList();
        Iterator var3 = appSenders.iterator();

        while(var3.hasNext()) {
            AppSender as = (AppSender)var3.next();
            randomReaders.add(new RandomReader(as, 1000L));
        }

        SequentialReaderExecutor sequentialReaderExecutor = new SequentialReaderExecutor();
        Iterator var9 = randomReaders.iterator();

        while(var9.hasNext()) {
            SensorsReader sr = (SensorsReader)var9.next();
            sequentialReaderExecutor.addReader(sr);
        }

        PrintService printService = new PrintService();
        WirelessSender wirelessSender = new WirelessSender(appSenders, this.xbeeReceiver);
        Iterator var6 = appSenders.iterator();

        while(var6.hasNext()) {
            AppComponent ac = (AppComponent)var6.next();
            ac.subscribeToService(printService);
            ac.subscribeToService(wirelessSender);
        }

        ExecutorService mainExecutor = Executors.newFixedThreadPool(4);
        mainExecutor.submit(wirelessSender.getXbeeSender());
        mainExecutor.submit(wirelessSender);
        mainExecutor.submit(sequentialReaderExecutor);
        mainExecutor.submit(printService);
        mainExecutor.shutdown();
    }
}
