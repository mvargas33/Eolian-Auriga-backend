package ApplicationLayer.SensorReading.CANReaders;

import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.SensorReading.SensorsReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Clase específica para leer datos del BMS por el Bus CAN
 */
public class BMSReader extends SensorsReader {

    public BMSReader(AppSender myComponent, long readingDelayInMS) {
        super(myComponent, readingDelayInMS);
    }

    void readMessage(String message) {
        // Revisar el checksum aca

        //System.out.println(message);
        // leer el mensaje
        l
        //System.out.println(msg.length);
        if (msg.length != 16){
            System.out.println(message);return;
        }
        System.out.print("ID: " + msg[2] + " MSG: ");
        for(int i=8 ; i< 16; i++){
            System.out.print(" " + msg[i]);
//        switch (msg[0])
        }System.out.println("");
//
//            //case "$GPRMC":
//
//            default:
//                System.out.println(msg);
//                break;
//        }
    }

    void startReading() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        // NOTA: primero hay que iniciar el can com en comando 'stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb'
        // (9600 es el baud rate)
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sudo /sbin/ip link set can0 up type can bitrate 500000;");
        stringBuilder.append("sudo /sbin/ip link set can1 up type can bitrate 500000;");
        stringBuilder.append("cd ./src/main/java/ApplicationLayer/SensorReading/CANReaders/linux-can-utils;");
        stringBuilder.append("gcc candump.c lib.c -o candump;");
        stringBuilder.append("./candump any;");
        processBuilder.command("bash", "-c", stringBuilder.toString());

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
//            BufferedReader error_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String error;
//            while ((error = error_reader.readLine()) != null) {
//                System.out.println(error);
//            }

            String line = null;
            while(true){
                while ((line = reader.readLine()) != null) {
                    //System.out.println(line);
                    readMessage(line);
                }
            }

//            //para ver si termino
//            int exitVal = process.waitFor();
//            if (exitVal == 0) {
//                System.out.println("Se cierra la lectura.");
//                System.exit(0);
//            } else {
//                //abnormal...
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public double[] read() {
        return new double[0];
    }

    public static void main(String[] args) {
        BMSReader bmsReader = new BMSReader(null, 100);
        bmsReader.startReading();
    }
}
