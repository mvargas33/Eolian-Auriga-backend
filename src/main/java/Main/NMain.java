package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class NMain {

    public static void main(String[] argv) {
        // segun entiendo, ahora deberia iniciar todos los channels
        // con sus appcomponents definidos y lanzarlos a correr en paralelo
        // con el mismo executor del main antiguo

        // usar servicio print service
        List<Service> serviceList = new ArrayList<Service>();
        PrintService printService = new PrintService();
        serviceList.add(printService);

        // crear componentes dummy
        //String id, double[] minimosConDecimal, double[] maximosConDecimal, String[] nombreParametros
        AppComponent ac1 = new AppComponent("AC1", new double[] {0}, new double[] {1}, new String[] {"D1"});
        AppComponent ac2 = new AppComponent("AC2", new double[] {0}, new double[] {1}, new String[] {"D2"});
        AppComponent ac3 = new AppComponent("AC3", new double[] {0}, new double[] {1}, new String[] {"D3"});
        List<AppComponent> acList = new ArrayList<AppComponent>();
        acList.add(ac1);
        acList.add(ac2);
        acList.add(ac3);

        // voy a partir haciendo uno para testchannel
        TestChannel tc = new TestChannel(acList, serviceList);

        // con esto solo quedaria iniciar los channels con sus respectivos app components + el print service
        // y luego lanzar cada uno a un thread en el executer + el printService
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);
        mainExecutor.submit(printService);
        mainExecutor.submit(tc);
        mainExecutor.shutdown();
        // init canbus0

        //init gps

        //init i2c


    }
}
