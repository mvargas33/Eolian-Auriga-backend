package ApplicationLayer.SensorReading.GPSReader;
import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.SensorReading.SensorsReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;


/**
 * GPSReader lee los datos del
 */

public class GPSReader extends SensorsReader {
    //temporal, por ahora solo va a leer longitud y latitud
    // orden de values -> [decimales_latitud, angulo_latitud, orientacion_latitud, decimales_longitud, angulo_longitud, orientacion_longitud]
    // las orientaciones se reciben como N/E/S/W pero se trabajan usando la siguiente transformacion
    // 1 == N (North), -1 == S (South)
    // 1 == E (East), -1 == W (West)

    private double[] values = new double[6];

    public GPSReader(AppSender myComponent, long readingDelayInMS) {
        super(myComponent, readingDelayInMS);
    }

    /**
     * Simple checksum calculation for a NMEA message.
     * @param msg The message to check.
     * @return true if the coded message equals the checksum, false otherwise.
     */

    public boolean checkSum(String msg) {
        // se ignoran estos 2 caracteres
        String newMsg = msg.replace("I", "");
        newMsg = newMsg.replace("$", "");
        // el 0 es el mensaje y el 1 es el codigo checksum
        String[] msg_cs = newMsg.split("\\*");

        int result = 0;
        for(int i = 0; i < msg_cs[0].length(); i++) {
            result ^= msg_cs[0].charAt(i);
        }

        int checksum = Integer.parseInt(msg_cs[1], 16);

        return checksum == result;
    }

    public void RMCReader(String[] msg) {
        values[0] = Double.parseDouble(msg[3].substring(0, 2));
        values[1] = Double.parseDouble(msg[3].substring(2));
        switch (msg[4]) {
            case "N":
                values[2] = 1;
                break;
            case "S":
                values[2] = -1;
                break;
            default:
                System.out.println("ERROR: Valor "+msg[4]+" no identificado como direccion de latitud.");
        }
        values[3] = Double.parseDouble(msg[5].substring(0, 3));
        values[4] = Double.parseDouble(msg[5].substring(3));
        switch (msg[6]) {
            case "E":
                values[5] = 1;
                break;
            case "W":
                values[5] = -1;
                break;
            default:
                System.out.println("ERROR: Valor "+msg[6]+" no identificado como direccion de longitud.");
                // todo: no se si un mensaje de aviso basta o es mejor tirar un error.
        }
    }

    public void GGAReader(String[] msg) {
        values[0] = Double.parseDouble(msg[2].substring(0, 2));
        values[1] = Double.parseDouble(msg[2].substring(2));
        switch (msg[3]) {
            case "N":
                values[2] = 1;
                break;
            case "S":
                values[2] = -1;
                break;
            default:
                System.out.println("ERROR: Valor "+msg[4]+" no identificado como direccion de latitud.");
        }
        values[3] = Double.parseDouble(msg[4].substring(0, 3));
        values[4] = Double.parseDouble(msg[4].substring(3));
        switch (msg[5]) {
            case "E":
                values[5] = 1;
                break;
            case "W":
                values[5] = -1;
                break;
            default:
                System.out.println("ERROR: Valor "+msg[5]+" no identificado como direccion de longitud.");
                // todo: no se si un mensaje de aviso basta o es mejor tirar un error.
        }
        this.values = values;
    }

    public void VTGReader(String[] msg) {
        System.out.println("Mensaje VTG no requerido.");
    }

    public void GSVReader(String[] msg) {
        System.out.println("Mensaje GSV no requerido.");
    }

    public void GSAReader(String[] msg) {
        System.out.println("Mensaje GSA no requerido.");
    }

    public void readMessage(String message) {
        // Revisar el checksum aca
        if(checkSum(message)) {
            // leer el mensaje
            String[] msg = message.split(",");
            switch (msg[0]) {
                // esto se podria hacer con una clase 'NMEAsentenceReader' implementada para cada tipo de mensaje, pero no se
                // que tanto valga la pena, primero se planea implementar asi, luego evaluar si es mejor abstraerlo mas.
                case "$GPRMC":
                    RMCReader(msg);
                    break;
                case "$GPGGA":
                    GGAReader(msg);
                    break;
                case "$GPGSV":
                    GSVReader(msg);
                    break;
                case "$GPVTG":
                    VTGReader(msg);
                    break;
                case "$GPGSA":
                    GSAReader(msg);
                    break;
                default:
                    System.out.println("Mensaje, "+msg[0]+" no soportado para lectura");
                    break;
            }
        }
        // si falla el checksum avisar (pero no terminar el programa)
        else {
            System.out.println("Checksum requirements were not met, message ignored.");
        }
    }

    void startReading() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // NOTA: primero hay que iniciar el serial com en comando 'stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb'
        // (9600 es el baud rate)
        processBuilder.command("bash", "-c", "stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb");
        processBuilder.command("bash", "-c", "cat /dev/serial0");

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
        //pendiente aplicar la logica requerida (leer cada tantos ms)
        return values;
    }

    public static void main(String[] argv) {
        /* Pasar esto a tests unitarios "Test Reader"
        String[] RMCmsg = "$GPRMC,215829.000,A,3526.9451,S,07140.3300,W,0.32,349.15,030221,,,A*64".split(",");
        double RMCresult[] = RMCReader(RMCmsg);
        for (double num : RMCresult) {
            System.out.print(num);
            System.out.print(' ');
        }

        System.out.println();

        String[] GGAmsg = "$GPGGA,215830.000,3526.9450,S,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B".split(",");
        double GGAresult[] = GGAReader(GGAmsg);
        for (double num : GGAresult) {
            System.out.print(num);
            System.out.print(' ');
        } */

        // pasar esto a test
        //String GGAmsg = "$GPGGA,215830.000,3526.9450,S,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B";
        //System.out.println(checkSum(GGAmsg));
    }
}
