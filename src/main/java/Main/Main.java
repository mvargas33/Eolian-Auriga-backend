//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.Canbus1;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.DatabaseService;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WebSocketService;
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import io.socket.engineio.client.transports.WebSocket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.system.SystemInfo;

public class Main {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/components/auriga";
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

        System.out.println("Java Version      :  " + SystemInfo.getJavaVersion());
        if(!SystemInfo.getJavaVersion().equals("1.8.0_212")) {
            System.out.println("WARNING: Java version should be 1.8.0_212, the current version is "+SystemInfo.getJavaVersion());
        }
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
        System.out.println("Main Sender");
        
        //List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(dir);
        List<AppComponent> lac = new ArrayList<>();
        AppComponent ac = new AppComponent("sevcon", new double[] {0, 1, 2}, new double[] {9, 19, 29}, new String[] {"rpm", "torque", "fault"});
        AppComponent ac2 = new AppComponent("sevc2on", new double[] {0, 1, 2}, new double[] {9, 19, 29}, new String[] {"rpm", "torque", "fault"});
        lac.add(ac);
        lac.add(ac2);
        List<Service> ls = new ArrayList<>();
        
        PrintService ps = new PrintService("M: ");
        WebSocketService wss = new WebSocketService();
        DatabaseService db = new DatabaseService(lac);
        ls.add(ps);
        ls.add(wss);
        ls.add(db);

        TestChannel reader = new TestChannel(lac, ls);
    
        
        Thread t1 = new Thread(reader);
        Thread t2 = new Thread(ps);
        Thread t4 = new Thread(wss);
        Thread t3 = new Thread(db);
        t1.start();
        t3.start();
        //t4.start();
        //t1.start();
        //t2.start();

    }
}
