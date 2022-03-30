//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.Canbus0;
import ApplicationLayer.Channel.Canbus0_real;
import ApplicationLayer.Channel.Canbus1;
import ApplicationLayer.Channel.GPSChannel;
import ApplicationLayer.Channel.I2C;
import ApplicationLayer.Channel.I2CTemp;
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

public class Temp {
    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";
    public XbeeReceiver xbeeReceiver;

    public Temp() {
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

        System.out.println("Java Version      :  " + SystemInfo.getJavaVersion());
        if(!SystemInfo.getJavaVersion().equals("1.8.0_212")) {
            System.out.println("WARNING: Java version should be 1.8.0_212, the current version is "+SystemInfo.getJavaVersion());
        }
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
        System.out.println("Main Sender");
        
        // Components
        List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents(componentsPath);
        
        // Services
        List<Service> ls = new ArrayList<>();

        // Channels
        I2CTemp temp = new I2CTemp(lac,ls);
        // Main loops
        Thread t1 = new Thread(temp);
        t1.start();
    }
}
