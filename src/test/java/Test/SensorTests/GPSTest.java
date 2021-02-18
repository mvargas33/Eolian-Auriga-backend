package Test.SensorTests;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class GPSTest {
    /*
    public void parseGPRMC(String[] msg) throws Exception{
        // Check if data is valid
        if (msg[2].equals("V")){
            throw new Exception("GPS: Data from message " + " is not valid");
        }

        double [] values = new double [];
        latitud = Double.parseDouble(msg[3]);
        switch (msg[2]) {
            case "N":
                lat_dir = 0;
                break;
            case "S":
                lat_dir = 1;
                break;
            default:
                System.out.println("ERROR: Valor "+msg[2]+" no identificado como direccion de latitud.");
        }
        longitud = Double.parseDouble(msg[3]);
        switch (msg[4]) {
            case "E":
                long_dir = 0;
                break;
            case "W":
                long_dir = 1;
                break;
            default:
                System.out.println("ERROR: Valor "+msg[4]+" no identificado como direccion de longitud.");
                // todo: no se si un mensaje de aviso basta o es mejor tirar un error.
        }
    }

    void readMessage(String message) {
        String[] msg = message.split(",");
        switch (msg[0]) {
            // esto se podria hacer con una clase 'NMEAsentenceReader' implementada para cada tipo de mensaje, pero no se
            // que tanto valga la pena, primero se planea implementar asi, luego evaluar si es mejor abstraerlo mas.
            case "$GPGGA":
                //System.out.println(Array.print);
                Arrays.stream(msg).forEach(System.out::print);
                break;
            case "$GPGSA":
                Arrays.stream(msg).forEach(System.out::print);
                break;
            case "$GPVTG":
                Arrays.stream(msg).forEach(System.out::print);
                break;
            case "$GPRMC":
                Arrays.stream(msg).forEach(System.out::print);
                break;
            case "$GPGSV":
                Arrays.stream(msg).forEach(System.out::print);
                break;
//            case "$GPVBW":
//                System.out.println(msg);
//                break;
            default:
                System.out.println("Mensaje, "+msg[0]+" no soportado para lectura");
                break;
        }
    }

    @Test
    public void simpleRead() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        //processBuilder.command("bash", "-c", "stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb;");
        processBuilder.command("bash", "-c", "cat /dev/serial0");

        ArrayList<String> msgs = new ArrayList<String>();

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
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

        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    */
}
