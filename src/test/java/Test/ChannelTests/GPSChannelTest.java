package Test.ChannelTests;

import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.GPS;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GPSChannelTest {

    void readingTest() throws Exception {
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_auriga";
        List<AppSender> allAppSenders = CSVToAppComponent.CSVs_to_AppSenders(dir);
        AppSender gps = null;
        for (AppSender a: allAppSenders
             ) {
            if(a.ID.equals("gps")){ // Must match xlsx name
                gps = a;
                break;
            }
        }
        if(gps == null){
            throw new Exception("AppSender for 'gps' not found after CSVs_to_AppSenders() function");
        }

        List<AppSender> appSenders = new ArrayList<>();
        appSenders.add(gps);

        List<Service> services = new ArrayList<>();
        PrintService printService = new PrintService();
        services.add(printService);

        GPS gpsChannel = new GPS(appSenders, services);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);

        // Init threads
        mainExecutor.submit(printService);
        mainExecutor.submit(gpsChannel);

        mainExecutor.shutdown();
    }

    public static void main(String[] args) {
        GPSChannelTest test = new GPSChannelTest();
        try{
            test.readingTest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
