package ApplicationLayer.SensorReading.CANReaders;

import ApplicationLayer.AppComponents.AppSender;
import ApplicationLayer.SensorReading.SensorsReader;
import ApplicationLayer.SensorReading.Utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Clase específica para leer datos del BMS por el Bus CAN
 */
public class BMSReader extends SensorsReader {
    private byte[] data = new byte[8];
    private int[] voltages = new int[256];
    private int[] temp_all_the_time = new int[256];
    private int[] temp_when_balacing_off = new int[256];
    private int[] resistance = new int[256];

    private int[] voltage_reading_ok = new int[256];
    private int[] temperature_reading_ok = new int[256];
    private int[] resistance_reading_ok = new int[256];
    private int[] load_is_on = new int[256];
    private int[] voltage_sensor_fault = new int[256];
    private int[] temperature_sensor_fault = new int[256];
    private int[] resistance_calculation_fault = new int[256];
    private int[] load_fault = new int[256];

    public BMSReader(AppSender myComponent, long readingDelayInMS) {
        super(myComponent, readingDelayInMS);
    }

    void readMessage(String message) {
        String[] msg = Utils.split(message, " "); // Better performance split than String.split()

        if (msg.length != 16){ // If it isn't CAN-type message
            System.out.println(message);
            return;
        }

        // Parse HEX strings to byte data type
        for(int i=8 ; i< 16; i++){
            data[i-8] = (byte) ((Character.digit(msg[i].charAt(0), 16) << 4) + (Character.digit(msg[i].charAt(1), 16)));
        }

        switch (msg[2]){
            case "622":
                // State of system
                int fault_state  = data[0] & 0b00000001;
                int K1_contactor = data[0] & 0b00000010;
                int K2_contactor = data[0] & 0b00000100;
                int K3_contactor = data[0] & 0b00001000;

                int powerup_time = ((data[1] << 8) & data[2]);

                // Byte of flags
                int power_from_source = data[3] & 0b00000001;
                int power_from_load = data[3] & 0b00000010;
                int interlock_tripped = data[3] & 0b00000100;
                int hard_wire_contactor_request = data[3] & 0b00001000;
                int can_contactor_request = data[3] & 0b00010000;
                int HLIM_set = data[3] & 0b00100000;
                int LLIM_set = data[3] & 0b01000000;
                int fan_on = data[3] & 0b10000000;

                // Fault code, stored
                int fault_code = data[4];

                // Level fault flags
                int driving_off_while_plugged_in = data[5] & 0b00000001;
                int interlock_tripped2 = data[5] & 0b00000010;
                int comm_fault_blank_or_cell = data[5] & 0b00000100;
                int charge_overcurrent = data[5] & 0b00001000;
                int discharge_overcurrent = data[5] & 0b00010000;
                int over_temp = data[5] & 0b00100000;
                int under_voltage = data[5] & 0b01000000;
                int over_voltage = data[5] & 0b10000000;

                // Warnings flags
                int low_voltage = data[6] & 0b00000001;
                int high_voltage = data[6] & 0b00000010;
                int charge_overcurrent_warning = data[6] & 0b00000100;
                int discharge_overcurrent_warning = data[6] & 0b00001000;
                int cold_temp = data[6] & 0b00010000;
                int hot_temp = data[6] & 0b00100000;
                int low_SOH = data[6] & 0b01000000;
                int isolateion_fault = data[6] & 0b10000000;

            case "623":
                int pack_voltage = ((data[0] << 8) & data[1]); // [kV]
                int min_voltage = data[2]; // [100mV]
                int min_voltage_id = data[3];
                int max_voltage = data[4];
                int max_voltage_id = data[5];

            case "624":
                int current = ((data[0] << 8) & data[1]); // [A] signed!
                int charge_limit = ((data[2] << 8) & data[3]); // [A]
                int discharge_limit = ((data[4] << 8) & data[5]); // [A]

            case "625":
                int batt_energy_in  = (data[0] << 8*3) & (data[1] << 8*2) & (data[2] << 8) & data[3]; // [kWh]
                int batt_energy_out = (data[4] << 8*3) & (data[5] << 8*2) & (data[6] << 8) & data[7];

            case "626":
                int SOC = data[0];
                int DOD = ((data[1] << 8) & data[2]);
                int capacity = ((data[3] << 8) & data[4]);
                int SOH = data[6];

            case "627":
                int temperature = data[0];
                int min_temp = data[2];
                int min_temp_id = data[3];
                int max_temp = data[4];
                int max_temp_id = data[5];

            case "628":
                int pack_resistance = ((data[0] << 8) & data[1]);
                int min_resistance = data[2];
                int min_resistance_id = data[3];
                int max_resistance = data[4];
                int max_resistance_id = data[5];

            default:
                int BASE_DUMP_ID = 0;
                int id = Integer.parseInt(msg[2]);

                // Individual cell details
                if (id == BASE_DUMP_ID){
                    this.voltages[data[0]] = data[1]; // mV
                    this.temp_all_the_time[data[0]] = data[2]; // °C
                    this.temp_when_balacing_off[data[0]] = data[3]; // °C
                    this.resistance[data[0]] = data[4]; // 100 uOhm
                    // Status
                    this.voltage_reading_ok[data[0]] = data[5] & 0b00000001;
                    this.temperature_reading_ok[data[0]] = data[5] & 0b00000010;
                    this.resistance_reading_ok[data[0]] = data[5] & 0b00000100;
                    this.load_is_on[data[0]] = data[5] & 0b00001000;
                    this.voltage_sensor_fault[data[0]] = data[5] & 0b00010000;
                    this.temperature_sensor_fault[data[0]] = data[5] & 0b00100000;
                    this.resistance_calculation_fault[data[0]] = data[5] & 0b01000000;
                    this.load_fault[data[0]] = data[5] & 0b10000000;
                }

                // Cell voltages
                else if (id > BASE_DUMP_ID && id <= (BASE_DUMP_ID+32)){
                    this.voltages[((id-BASE_DUMP_ID-1)*8)] = data[0];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+1] = data[1];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+2] = data[2];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+3] = data[3];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+4] = data[4];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+5] = data[5];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+6] = data[6];
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+7] = data[7];
                }

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
