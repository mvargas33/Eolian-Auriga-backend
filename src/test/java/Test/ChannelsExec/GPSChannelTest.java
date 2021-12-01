package Test.ChannelsExec;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.GPSChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GPSChannelTest {

    void readingTest() throws Exception {
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_auriga";
        List<AppComponent> allAppComponents = CSVToAppComponent.CSVs_to_AppComponents(dir);
        AppComponent gps = null;
        for (AppComponent a: allAppComponents
             ) {
            if(a.ID.equals("gps")){ // Must match xlsx name
                gps = a;
                break;
            }
        }
        if(gps == null){
            throw new Exception("AppComponent for 'gps' not found after CSVs_to_AppComponents() function");
        }

        List<AppComponent> appSenders = new ArrayList<>();
        appSenders.add(gps);

        List<Service> services = new ArrayList<>();
        PrintService printService = new PrintService();
        services.add(printService);

        GPSChannel gpsChannel = new GPSChannel(appSenders, services);

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
