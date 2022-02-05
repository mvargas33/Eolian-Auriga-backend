//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.Canbus0;
import ApplicationLayer.Channel.Canbus1;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.*;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeSender;
import io.socket.engineio.client.transports.WebSocket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.system.SystemInfo;

public class MainSender {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";
    public XbeeReceiver xbeeReceiver;

    public MainSender() {
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
        System.out.println("Main Sender");
        
        // Components
        List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(args[1]);
        
        // Services
        List<Service> ls = new ArrayList<>();
        WirelessSender ws = new WirelessSender(lac, args[0], false);
        //PrintService ps = new PrintService("TX: ");
        WebSocketService wss = new WebSocketService();
        //LCDScreen1 lcd1 = new LCDScreen1(0x26); //ver si las lcd van o no, para no gastar threads
        //LCDScreen2 lcd2 = new LCDScreen2(0x25);
        DatabaseService dbs = new DatabaseService(lac);

        ls.add(ws);
        //ls.add(lcd1);
        //ls.add(lcd2);
        //ls.add(ps);
        ls.add(wss);
        ls.add(dbs);

        // Channels
        Canbus1 can1 = new Canbus1(lac, ls);
        Canbus0 can0 = new Canbus0(lac, ls);

        // Main loops
        Thread t1 = new Thread(can1);
        Thread t5 = new Thread(can0);
        //Thread t6 = new Thread(lcd1);
        //Thread t7 = new Thread(lcd2);
        //Thread t2 = new Thread(ps);
        Thread t3 = new Thread(ws);
        Thread t4 = new Thread(wss);
        Thread t9 = new Thread(dbs);
        t1.start();
        t5.start();
        //t6.start();
        //t7.start();
        //t2.start();
        t3.start();
        t4.start(); 
        t9.start();
    }
}
