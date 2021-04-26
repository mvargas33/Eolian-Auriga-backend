package ApplicationLayer.SensorReading.MPPTReader;

import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.SensorReading.SensorsReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MPPTReader extends SensorsReader {

    //todo enviar por i2c
    //evaluar si mejorar esto o dejarlo asi

    //primer mppt (id 771)
    public int Uin_1;
    public int Iin_1;
    public int Uout_1;
    public int uout_umax_1;
    public int t_cooler_1;
    public int bateria_1;
    public int under_volt_1;
    public int temp_1;

    //2do mppt (id 772)
    public int Uin_2;
    public int Iin_2;
    public int Uout_2;
    public int uout_umax_2;
    public int t_cooler_2;
    public int bateria_2;
    public int under_volt_2;
    public int temp_2;

    private double[] values = new double[6];

    public MPPTReader(AppSender myComponent, long readingDelayInMS) {
        super(myComponent, readingDelayInMS);
    }

    //Ejemplo
    // can0 771 [8] FF FF FF FF FF FF FF FF
    public void readMessage(String message) {
        String[] msg = message.split("\\s+");

        // guardar el mensaje menos "[canal] [id] [bytes]"
        int[] bytes = new int[msg.length - 3];
        for(int i = 3; i < msg.length; i++) {
            System.out.println(msg[i]);
            bytes[i-3] = Integer.parseInt(msg[i], 16);
            System.out.println(bytes[i-3]);
        }
        //revisar los id
        //en este punto bytes es el "buff" de la version del fenix
        if(msg[1] == "771") {
            this.Uin_1 = ((bytes[0] & 0b00000011) << 6) | (bytes[1]); //preguntar esto
            this.Iin_1 = ((bytes[2] & 0b00000011) << 6) | (bytes[3]); //preguntar esto
            this.Uout_1 = ((bytes[4] & 0b00000011) << 6) | (bytes[5]); //preguntar esto
            this.uout_umax_1 = bytes[0] & 0b10000000;
            this.t_cooler_1 = bytes[0] & 0b01000000;
            this.bateria_1 = bytes[0] & 0b00100000;
            this.under_volt_1 = bytes[0] & 0b00010000;
            this.temp_1 = bytes[6];
            //el ultimo byte no se usa para nada?
        }
        else if (msg[1] == "772") {
            this.Uin_2 = ((bytes[0] & 0b00000011) << 6) | (bytes[1]); //preguntar esto
            this.Iin_2 = ((bytes[2] & 0b00000011) << 6) | (bytes[3]); //preguntar esto
            this.Uout_2 = ((bytes[4] & 0b00000011) << 6) | (bytes[5]); //preguntar esto
            this.uout_umax_2 = bytes[0] & 0b10000000;
            this.t_cooler_2 = bytes[0] & 0b01000000;
            this.bateria_2 = bytes[0] & 0b00100000;
            this.under_volt_2 = bytes[0] & 0b00010000;
            this.temp_2 = bytes[6];
        }
    }

    void startReading() {
        ProcessBuilder processBuilder = new ProcessBuilder();

        //enviar mensaje para recibir los parametros, modificar los argumentos
        processBuilder.command("bash", "-c", "candump any");

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
        //propuestas de tests
        //readMessage("can0 771 [8] FF FF FF FF 00 10 FF FF");
    }
}
