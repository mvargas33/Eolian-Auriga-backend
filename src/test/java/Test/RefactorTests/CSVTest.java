package Test.RefactorTests;

import ApplicationLayer.AppComponents.AppReceiver;
import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import org.junit.jupiter.api.Test;

import java.util.List;


public class CSVTest {

    @Test
    public void staticTest() throws Exception{
        String dir = "src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";

        /*List<String> files = CSVToAppComponent.listFilesForFolder(dir);
        for (String file: files
             ) {
            System.out.println(file);
        }*/


        List<AppSender> appSenders = CSVToAppComponent.CSVs_to_AppSenders(dir);

        for (AppSender appSender: appSenders
             ) {
            System.out.println(appSender.toString());
        }

        List<AppReceiver> appReceivers = CSVToAppComponent.CSVs_to_AppReceivers(dir);
    }


}
