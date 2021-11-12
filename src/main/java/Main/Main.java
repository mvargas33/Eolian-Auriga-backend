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
import ApplicationLayer.LocalServices.WirelessService.WirelessReceiver;
import ApplicationLayer.LocalServices.WirelessService.WirelessSender;
import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeReceiver;
import io.socket.engineio.client.transports.WebSocket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.system.SystemInfo;

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

        System.out.println("Java Version      :  " + SystemInfo.getJavaVersion());
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
        
        // List<AppComponent> lac = new ArrayList<>();
        // List<Service> ls = new ArrayList<>();

        // AppComponent ac = new AppComponent("A",
        //  new double[] {0, 0, 0, 0}, //min cd
        //  new double[] {10, 1, 1, 1}, //max cd
        //  new String[] {"A1" , "A2", "A3", "A4"});

        // //WirelessReceiver wr = new WirelessReceiver(lac, "COM6", false, ls);
        // PrintService ps = new PrintService();
        
        // lac.add(ac);
        // ls.add(ps);
        
        // WirelessReceiver wrr = new WirelessReceiver(lac,  false, ls);
        // WirelessSender wrs = new WirelessSender(lac, wrr.getXbeeReceiver(), false);
        // ls.add(wrs);
        // TestChannel tc = new TestChannel(lac, ls);

        // Thread t1 = new Thread(tc);
        // Thread t2 = new Thread(ps);
        // Thread t3 = new Thread(wrr);
        // Thread t4 = new Thread(wrs);
        // t1.start();
        // //t2.start();
        // t3.start();
        // t4.start();
        AppComponent acSND = new AppComponent("A",
        new double[] {0, 0, 0, 0}, //min cd
        new double[] {10, 1, 1, 1}, //max cd
        new String[] {"A1" , "A2", "A3", "A4"});

        AppComponent acRCV = new AppComponent("A",
        new double[] {0, 0, 0, 0}, //min cd
        new double[] {10, 1, 1, 1}, //max cd
        new String[] {"A1" , "A2", "A3", "A4"});

        List<AppComponent> appComponentsSND = new ArrayList<>();
        List<AppComponent> appComponentsRCV = new ArrayList<>();

        appComponentsRCV.add(acRCV);
        appComponentsSND.add(acSND);

        // WirelessReceiver
        List<Service> otherServices = new ArrayList<>();
        PrintService psRCV = new PrintService();
        otherServices.add(psRCV);
        WirelessReceiver wirelessReceiver = new WirelessReceiver(appComponentsRCV,false, otherServices);

        // WirelessSender
        List<Service> sendServices = new ArrayList<>();
        PrintService psSND = new PrintService();
        sendServices.add(psSND);
        WirelessSender wirelessSender = new WirelessSender(appComponentsSND, wirelessReceiver.getXbeeReceiver(), false);
        sendServices.add(wirelessSender);

        TestChannel testChannel = new TestChannel(appComponentsSND, sendServices);

        for(AppComponent a : testChannel.myComponentList){
            wirelessSender.serve(a);
        }
        

        wirelessReceiver.processMsg();
        // Channel that informs to WirelessSender
        Thread t1 = new Thread(testChannel);
        Thread t4 = new Thread(psRCV);
        Thread t5 = new Thread(psSND);
        Thread t2 = new Thread(wirelessReceiver);
        Thread t3 = new Thread(wirelessReceiver);
        t1.start();
        t4.start();
        t2.start();
        t3.start();
        //t5.start();

    }
}
