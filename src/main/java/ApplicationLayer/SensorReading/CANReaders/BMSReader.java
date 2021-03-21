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

        // leer el mensaje
        String[] msg = message.split(" ");
        switch (msg[0]) {

            //case "$GPRMC":

            default:
                System.out.println(msg);
                break;
        }
    }

    void startReading() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // NOTA: primero hay que iniciar el can com en comando 'stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb'
        // (9600 es el baud rate)
        processBuilder.command("bash", "-c", "sudo /sbin/ip link set can0 up type can bitrate 500000");
        processBuilder.command("bash", "-c", "sudo /sbin/ip link set can1 up type can bitrate 500000");
        processBuilder.command("bash", "-c", "gcc ./candump.c");
        processBuilder.command("bash", "-c", "./candump any");

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                readMessage(line);
            }

            //para ver si termino
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Se cierra la lectura.");
                System.exit(0);
            } else {
                //abnormal...
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
