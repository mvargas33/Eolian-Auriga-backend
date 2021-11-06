//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WebSocketService;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import io.socket.engineio.client.transports.WebSocket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";
    public XbeeReceiver xbeeReceiver;

    public Main() {
    }

    public static AppComponent findAppComponent(List<AppComponent> list, String componentID) throws Exception {
        Iterator var2 = list.iterator();

        AppComponent a;
        do {
            if (!var2.hasNext()) {
                throw new Exception("Component with ID " + componentID + " was not found in AppComponent list.");
            }

            a = (AppComponent)var2.next();
        } while(!a.getID().equals(componentID));

        return a;
    }

    public static void main(String[] args) throws Exception {
        List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(dir);
        List<Service> ls = new ArrayList<>();
        PrintService ps = new PrintService();
        WebSocketService wss = new WebSocketService();
        ls.add(ps);
        ls.add(wss);

        TestChannel tc = new TestChannel(lac, ls);
        Thread t1 = new Thread(tc);
        Thread t2 = new Thread(ps);
        Thread t3 = new Thread(wss);
        t1.start();
        t2.start();
        t3.start();
        Thread.sleep(10000);
    }
}
