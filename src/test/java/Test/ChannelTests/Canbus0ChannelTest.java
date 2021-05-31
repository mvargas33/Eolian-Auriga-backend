package Test.ChannelTests;

import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.Canbus0;
import ApplicationLayer.Channel.GPS;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Canbus0ChannelTest {

    void readingTest() throws Exception {
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_auriga";
        List<AppSender> allAppSenders = CSVToAppComponent.CSVs_to_AppSenders(dir);
        AppSender bms = null;
        for (AppSender a: allAppSenders
        ) {
            if(a.ID.equals("bms")){ // Must match xlsx name
                bms = a;
                break;
            }
        }
        if(bms == null){
            throw new Exception("AppSender for 'bms' not found after CSVs_to_AppSenders() function");
        }

        List<AppSender> appSenders = new ArrayList<>();
        appSenders.add(bms);

        List<Service> services = new ArrayList<>();
        PrintService printService = new PrintService();
        services.add(printService);

        Canbus0 gpsChannel = new Canbus0(appSenders, services);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);

        // Init threads
        mainExecutor.submit(printService);
        mainExecutor.submit(gpsChannel);

        mainExecutor.shutdown();
    }

    public static void main(String[] args) {
        Canbus0ChannelTest test = new Canbus0ChannelTest();
        try{
            test.readingTest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
