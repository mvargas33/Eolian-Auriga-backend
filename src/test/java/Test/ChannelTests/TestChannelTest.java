package Test.ChannelTests;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.SensorReading.SensorsReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestChannelTest {

    void readingTest() throws Exception {
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_auriga";
        List<AppSender> appSenders = CSVToAppComponent.CSVs_to_AppSenders(dir);
        List<Service> services = new ArrayList<>();
        PrintService printService = new PrintService();
        services.add(printService);

        TestChannel testChannel = new TestChannel(appSenders, services);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);

        // Init threads
        mainExecutor.submit(printService);
        mainExecutor.submit(testChannel);

        mainExecutor.shutdown();
    }

    public static void main(String[] args) {
        TestChannelTest test = new TestChannelTest();
        try{
            test.readingTest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
