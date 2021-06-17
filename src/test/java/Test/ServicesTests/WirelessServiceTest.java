package Test.ServicesTests;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import Test.ChannelTests.TestChannelTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WirelessServiceTest {

    void mainTest() throws Exception {
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_auriga";
        List<AppComponent> appComponents = CSVToAppComponent.CSVs_to_AppComponents(dir);

        // WirelessReceiver + Printer
        List<Service> otherServices = new ArrayList<>();
        PrintService printServiceReceive = new PrintService("Rcv |");
        otherServices.add(printServiceReceive);

        WirelessReceiver wirelessReceiver = new WirelessReceiver(appComponents,false, otherServices);

        // WirelessSender
        List<Service> sendServices = new ArrayList<>();
        PrintService printServiceSender = new PrintService("Snd |");
        WirelessSender wirelessSender = new WirelessSender(appComponents, wirelessReceiver.getXbeeReceiver(), false);
        sendServices.add(wirelessSender);
        sendServices.add(printServiceSender);

        // Channel that informs to WirelessSender
        TestChannel testChannel = new TestChannel(appComponents, sendServices);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(5);

        // Init threads
        mainExecutor.submit(printServiceReceive);
        mainExecutor.submit(wirelessReceiver);
        mainExecutor.submit(wirelessReceiver.getXbeeReceiver());
//        mainExecutor.submit(wirelessSender);
        mainExecutor.submit(testChannel);
        mainExecutor.submit(printServiceSender);


        wirelessSender.run();

        mainExecutor.shutdown();
    }

    public static void main(String[] args) {
        WirelessServiceTest test = new WirelessServiceTest();
        try{
            test.mainTest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
