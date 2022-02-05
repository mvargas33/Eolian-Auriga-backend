package Main;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.Canbus0;
import ApplicationLayer.Channel.TestChannel;
import ApplicationLayer.LocalServices.PrintService;
import ApplicationLayer.LocalServices.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class NMain {

    public static String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";

    public static void main(String[] argv) throws Exception {
        // segun entiendo, ahora deberia iniciar todos los channels
        // con sus appcomponents definidos y lanzarlos a correr en paralelo
        // con el mismo executor del main antiguo

        // usar servicio print service
        List<Service> serviceList = new ArrayList<>();
        PrintService printService = new PrintService();
        serviceList.add(printService);

        List<AppComponent> ACList = CSVToAppComponent.CSVs_to_AppComponents(dir);

        // voy a partir haciendo uno para testchannel
        TestChannel tc = new TestChannel(ACList, serviceList, new String[] {"BMS", "MPPT"});

        // con esto solo quedaria iniciar los channels con sus respectivos app components + el print service
        // y luego lanzar cada uno a un thread en el executer + el printService
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);
        mainExecutor.submit(printService);
        mainExecutor.submit(tc);
        mainExecutor.shutdown();
        // init canbus0

        // duda: Si usamos la conversion de csv/excel -> AppComponent como dice la linea abajo,
        // como se distingue que app component pertenece a cada channel?
        // Idea: Se podria agregar como un "channel index" a los excel/csv o un campo dentro de los channels
        // que guarde los ids (en el excel/csv) que tienen que guardar
        // Componentes: MPPT (desde el arduino?), BMS


        // init gps
        // Componentes: GPS

        // init i2c
        // Componentes: Arduino con los datos del MPPT

    }
}
