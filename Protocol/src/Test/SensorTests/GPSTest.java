package Test.SensorTests;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GPSTest {

    void readMessage(String message) {
        String[] msg = message.split(",");
        switch (msg[0]) {
            // esto se podria hacer con una clase 'NMEAsentenceReader' implementada para cada tipo de mensaje, pero no se
            // que tanto valga la pena, primero se planea implementar asi, luego evaluar si es mejor abstraerlo mas.
            case "$GPGLL":
                System.out.println(msg);
                break;
            case "$GPMDA":
                System.out.println(msg);
                break;
            case "$GPMWV":
                System.out.println(msg);
                break;
            case "$GPRMC":
                System.out.println(msg);
                break;
            case "$GPTLL":
                System.out.println(msg);
                break;
            case "$GPVBW":
                System.out.println(msg);
                break;
            default:
                System.out.println("Mensaje, "+msg[0]+" no soportado para lectura");
                break;
        }
    }

    @Test
    public void simpleRead() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "cat /dev/serial0");

        ArrayList<String> msgs = new ArrayList<String>();

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

        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
