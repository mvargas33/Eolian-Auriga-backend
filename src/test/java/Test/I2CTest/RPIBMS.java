package Test.I2CTest;

import java.util.ArrayList;
import java.util.List;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;

/**
 * Hello world!
 *
 */
public class RPIBMS {

    public static void main(String[] args) throws Exception {
    	PrintService ps = new PrintService();
    	List<Service> ls = new ArrayList<>();
    	ls.add(ps);
    	List<AppComponent> la = CSVToAppComponent.CSVs_to_AppComponents("bms");
        I2C rpi = new I2C(la, ls);
    }

}