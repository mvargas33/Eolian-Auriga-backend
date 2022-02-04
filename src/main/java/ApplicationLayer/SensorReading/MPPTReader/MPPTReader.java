package ApplicationLayer.SensorReading.MPPTReader;

import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.SensorReading.GPSReader.GPSReader;
import ApplicationLayer.SensorReading.SensorsReader;
import MockObjects.GPS;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;

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

    // recibe un byte[] de largo 10, el frame CAN "directo" (pasó por el arduino) del MPPT
    public void readMessage(byte[] bytes) {
        //revisar los id
        //en este punto bytes es el "buff" de la version del fenix
        if(bytes[1] == 0x771) {
            System.out.println("Actualizados los datos para el MPPT 0x771");
            this.Uin_1 = ((bytes[2] & 0b00000011) << 6) | (bytes[3]); //preguntar esto
            this.Iin_1 = ((bytes[4] & 0b00000011) << 6) | (bytes[5]); //preguntar esto
            this.Uout_1 = ((bytes[6] & 0b00000011) << 6) | (bytes[7]); //preguntar esto
            this.uout_umax_1 = bytes[2] & 0b10000000;
            this.t_cooler_1 = bytes[2] & 0b01000000;
            this.bateria_1 = bytes[2] & 0b00100000;
            this.under_volt_1 = bytes[2] & 0b00010000;
            this.temp_1 = bytes[8];
            //el ultimo byte no se usa para nada?
        }
        else if (bytes[1] == 0x772) {
            System.out.println("Actualizados los datos para el MPPT 0x772");
            this.Uin_2 = ((bytes[2] & 0b00000011) << 6) | (bytes[3]); //preguntar esto
            this.Iin_2 = ((bytes[4] & 0b00000011) << 6) | (bytes[5]); //preguntar esto
            this.Uout_2 = ((bytes[6] & 0b00000011) << 6) | (bytes[7]); //preguntar esto
            this.uout_umax_2 = bytes[2] & 0b10000000;
            this.t_cooler_2 = bytes[2] & 0b01000000;
            this.bateria_2 = bytes[2] & 0b00100000;
            this.under_volt_2 = bytes[2] & 0b00010000;
            this.temp_2 = bytes[8];
        }
    }

    // escucha continuamente a i2c y procesa lo que le llegue
    public void readI2CMessage() {
        try {
            System.out.println("Creating I2C bus");
            I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
            System.out.println("Creating I2C device");
            I2CDevice device = bus.getDevice(0x08);

            long waitTimeSent = 5000;
            long waitTimeRead = 1000;
            int cnt = 0;
            byte[] results = {(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
            while (true) {
                //negative values don't work
                System.out.println("Reading data via I2C");
                device.write((byte) ((cnt % 3)&0xFF));
                cnt += 1;
                device.read(results, 0, 10);
                System.out.println("Read via I2C ... Message");
                for(int i = 0; i < 10; i++) {
                    System.out.println(BitOperations.ArraytoString(results));
                    //System.out.println(results[i] & 0x00FF);
                }
                System.out.println("Procesando los datos del arduino i2c...");
                readMessage(results);
                System.out.println("Waiting 5 seconds");
                Thread.sleep(waitTimeRead);
            }
        } catch (IOException | I2CFactory.UnsupportedBusNumberException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }



    @Override
    public double[] read() {
        //pendiente aplicar la logica requerida (leer cada tantos ms)
        return values;
    }

    public static void main(String[] argv) {
        // propuesta para tests

        AppSender appSender = new AppSender("testing_AS",
                new double[] {0}, // mins
                new double[] {60}, //maxs
                new String[] {"DUMMY"});
        MPPTReader mpptReader = new MPPTReader(appSender, 3000);

        // con esto deberia printear los mensajes
        // "Actualizados los datos para el MPPT 0x771" o
        // "Actualizados los datos para el MPPT 0x772"
        mpptReader.readI2CMessage();

    }
}
