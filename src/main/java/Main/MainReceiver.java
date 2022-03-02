//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.NullChannel;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.DatabaseService;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WebSocketService;
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeSender;
//import io.socket.engineio.client.transports.WebSocket;
//import javafx.print.Printer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.system.SystemInfo;

public class MainReceiver {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/PC/components";
    public XbeeReceiver xbeeReceiver;

    public MainReceiver() {
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

        System.out.println("Java Version      :  " + SystemInfo.getJavaVersion());
        if(!SystemInfo.getJavaVersion().equals("1.8.0_212")) {
            System.out.println("WARNING: Java version should be 1.8.0_212, the current version is "+SystemInfo.getJavaVersion());
        }
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
        System.out.println("MainReceiver");
        
        List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(dir);
        List<Service> ls = new ArrayList<>();

        //WirelessReceiver wr = new WirelessReceiver(lac, "COM6", false, ls);
        //PrintService ps = new PrintService("RX: ");
        WebSocketService wss = new WebSocketService();
        //DatabaseService db = new DatabaseService(lac, "");
        
        //ls.add(ps);
        ls.add(wss);
        //ls.add(db);

        WirelessReceiver wr = new WirelessReceiver(lac, "COM3", false, ls);

        NullChannel nc = new NullChannel(lac, ls);

        Thread t1 = new Thread(nc);
        //Thread t2 = new Thread(ps);
        Thread t3 = new Thread(wr);
        Thread t4 = new Thread(wss);
        //Thread t5 = new Thread(db);
        System.out.println("Empezando lecturas en 5 segundos...");
        Thread.sleep(5000);
        t4.start();
        t1.start();
        //t2.start();
        t3.start();
        //t5.start();

    }
}
