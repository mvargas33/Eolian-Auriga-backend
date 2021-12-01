package ToReview.RefactorTests;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppComponent;
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


        List<AppComponent> appSenders = CSVToAppComponent.CSVs_to_AppComponents(dir);

        for (AppComponent appSender: appSenders
             ) {
            System.out.println(appSender.toString());
        }

        List<AppComponent> appComponents = CSVToAppComponent.CSVs_to_AppComponents(dir);
    }


}
