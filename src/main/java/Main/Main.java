//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.Canbus1;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.NullChannel;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.DatabaseService;
import ApplicationLayer.LocalServices.LCDScreen1;
import ApplicationLayer.LocalServices.LCDScreen2;
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

    public static int twoComp(int val, int msb) {
        if(msb == 31) {
            return val;
        }
        else {
            if(((val >> msb) & 1) == 1) {
                return -(1<<(msb+1))+val;
            }
            else {
                return val;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int test = 1;
        for(int i = 0; i < 32; i++) {
            test = (test << 1) | 1;
        }
        System.out.println(twoComp(test, 31)); // -1
        System.out.println(twoComp(2,16));  
        System.out.println(twoComp(3,1));
        boolean dev = false;
        boolean encrypt = false;
        String xbeePort = "/dev/ttyUSB0";
        String componentsPath = "/home/pi/Desktop/RPI/components/auriga/";
        String databasePath = "/home/pi/Desktop/RPI/";
        for(int i = 0; i < args.length; i++) {
            try {
                if(args[i].equals("--dev")) {
                    dev = true;
                }
                else if(args[i].equals("--xbee")) {
                    xbeePort = args[i+1];
                }
                else if(args[i].equals("--out")) {
                    databasePath = args[i+1];
                }
                else if(args[i].equals("--in")) {
                    componentsPath = args[i+1];
                }
                else if(args[i].equals("--encrypt")) {
                    encrypt = true;
                }
            }
            catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Usage: java -jar Main.jar [OPTIONS]");
                System.out.println("Options: --xbee <port>");
                System.out.println("         --out <path>");
                System.out.println("         --in <path>");
                System.out.println("         --dev");
                System.out.println("         --encrypt");
            }
        }
        if(!SystemInfo.getJavaVersion().equals("1.8.0_212")) {
            System.out.println("WARNING: Java version should be 1.8.0_212, the current version is "+SystemInfo.getJavaVersion());
        }
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
        System.out.println("Main Sender");
        
        //List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(args[1]);
        //List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(dir);
        //for(AppComponent c : lac) {
        //    if(c.getID().equals("lcd")) lac.remove(c);
        //}
        List<AppComponent> lac = new ArrayList<>();
        AppComponent ac = new AppComponent("sevcon", new double[] {0, 1, 2}, new double[] {9, 19, 29}, new String[] {"rpm", "torque", "fault"});
        AppComponent ac2 = new AppComponent("sevc2on", new double[] {0, 1, 2}, new double[] {9, 19, 29}, new String[] {"rpm", "torque", "fault"});
        lac.add(ac);
        lac.add(ac2);
        List<Service> ls = new ArrayList<>();
        
        PrintService ps = new PrintService("M: ");
        WebSocketService wss = new WebSocketService();
        //DatabaseService db = new DatabaseService(lac);
        // DatabaseService db = new DatabaseService(lac);
        //LCDScreen1 lcd1 = new LCDScreen1(0x27); //0x25
        //LCDScreen2 lcd2 = new LCDScreen2(0x27); //0x26
        //ls.add(lcd2);
        ls.add(ps);
        ls.add(wss);
        //ls.add(db);
        // ls.add(db);

        TestChannel reader = new TestChannel(lac, ls);
        //NullChannel nc = new NullChannel(lac, ls);
        
        
        //Thread t6 = new Thread(lcd1);
        //Thread t7 = new Thread(lcd2);
        Thread t1 = new Thread(reader);
        Thread t2 = new Thread(ps);
        Thread t4 = new Thread(wss);
        //Thread t3 = new Thread(db);
        //Thread t5 = new Thread(nc);
        
        //t6.start();
        t1.start();
        //t7.start();
        //t3.start();
        t4.start();
        // //t1.start();
        t2.start();
        //t5.start();

    }
}
