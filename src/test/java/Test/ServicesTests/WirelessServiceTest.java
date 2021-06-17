package Test.ServicesTests;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.State;
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import Test.ChannelTests.TestChannelTest;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class WirelessServiceTest {

    @org.junit.jupiter.api.Test
    void correctnessTest() throws Exception{
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Example";
        List<AppComponent> appComponentsSND = CSVToAppComponent.CSVs_to_AppComponents(dir);
        List<AppComponent> appComponentsRCV = CSVToAppComponent.CSVs_to_AppComponents(dir);

        // WirelessReceiver
        List<Service> otherServices = new ArrayList<>();
        WirelessReceiver wirelessReceiver = new WirelessReceiver(appComponentsRCV,false, otherServices);

        // WirelessSender
        List<Service> sendServices = new ArrayList<>();
        WirelessSender wirelessSender = new WirelessSender(appComponentsSND, wirelessReceiver.getXbeeReceiver(), false);
        sendServices.add(wirelessSender);

        // Channel that informs to WirelessSender
        TestChannel testChannel = new TestChannel(appComponentsSND, sendServices);

        // Setp-by-step test

        // 0. Create random values in range for all Components and save those values
        testChannel.randomValuesInRangeForAllComponents();
        List<AppComponent> appComponents_SND = testChannel.myComponentList; // Source

        // 1. Send over protocol, put bytes inn XbeeReceiver Queue
        for(AppComponent a : testChannel.myComponentList){
            wirelessSender.serve(a);
        }

        // 2. Poll from XbeeReceiver Queue and parse
        wirelessReceiver.processMsg();
        List<AppComponent> appComponents_RCV = wirelessReceiver.appCompUpdated; // Target


        // 3. Check values from appComponentsSND and appComponentsRCV, with bitSig tolerance
        // 3.1 Estimate what values should be in appComponentRCV
        HashMap<String, double[]> targetValuesMap = new HashMap<>();

        for (AppComponent c: appComponents_SND ) {
            // Do a compression - decompression of valoresRealesActuales[] (just a mapping to int[])
            State actState = wirelessSender.states.get(c.getID());
            // Compress
            int[] intVals = new int[actState.len];
            for (int i = 0; i < actState.len; i++) {
                intVals[i] = (int) Math.floor( c.valoresRealesActuales[i] * Math.pow(10, actState.decimales[i]) ) + actState.offset[i];
            }
            // Decompress
            double[] targetVals = new double[actState.len];
            for (int j = 0; j < actState.len; j++) {
                targetVals[j] = (intVals[j] - actState.offset[j]) * Math.pow(10, - actState.decimales[j]);
            }
            // Save in map
            targetValuesMap.put(c.ID, targetVals);
        }
        // 3.2 Check value per value, if the values match
        for(AppComponent c: appComponents_RCV){
            double[] target = targetValuesMap.get(c.ID);
            Assertions.assertArrayEquals(target, c.valoresRealesActuales);
        }


    }

    void useCaseTest() throws Exception {
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Example";
        List<AppComponent> appComponentsSND = CSVToAppComponent.CSVs_to_AppComponents(dir);
        List<AppComponent> appComponentsRCV = CSVToAppComponent.CSVs_to_AppComponents(dir);

        // WirelessReceiver + Printer
        List<Service> otherServices = new ArrayList<>();
        PrintService printServiceReceive = new PrintService("Rcv |");
        otherServices.add(printServiceReceive);

        WirelessReceiver wirelessReceiver = new WirelessReceiver(appComponentsRCV,false, otherServices);

        // WirelessSender
        List<Service> sendServices = new ArrayList<>();
        PrintService printServiceSender = new PrintService("Snd |");
        WirelessSender wirelessSender = new WirelessSender(appComponentsSND, wirelessReceiver.getXbeeReceiver(), false);
        sendServices.add(wirelessSender);
        sendServices.add(printServiceSender);

        // Channel that informs to WirelessSender
        TestChannel testChannel = new TestChannel(appComponentsSND, sendServices);

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
            test.correctnessTest();
            //test.useCaseTest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
