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
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WebSocketService;
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeSender;
import io.socket.engineio.client.transports.WebSocket;
import javafx.print.Printer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.system.SystemInfo;

public class MainReceiver {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";
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
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
        System.out.println("MainReceiver");
        
        List<AppComponent> lac = new ArrayList<>();
        List<Service> ls = new ArrayList<>();

        AppComponent ac = new AppComponent("A",
         new double[] {0, 0, 0, 0}, //min cd
         new double[] {20, 25, 34, 24}, //max cd
         new String[] {"A1" , "A2", "A3", "A4"});

        //WirelessReceiver wr = new WirelessReceiver(lac, "COM6", false, ls);
        PrintService ps = new PrintService();
        
        lac.add(ac);
        ls.add(ps);
        
        WirelessReceiver wr = new WirelessReceiver(lac, "COM6", false, ls);
        NullChannel nc = new NullChannel(lac, ls);

        Thread t1 = new Thread(nc);
        Thread t2 = new Thread(ps);
        Thread t3 = new Thread(wr);
        t1.start();
        t2.start();
        t3.start();


    }
}
