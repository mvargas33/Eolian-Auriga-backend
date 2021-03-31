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
    private double[] voltages = new double[28];
    private double[] temp_all_the_time = new double[28];
    private double[] temp_when_balacing_off = new double[28];
    private double[] resistance = new double[28];

    private double[] voltage_reading_ok = new double[28];
    private double[] temperature_reading_ok = new double[28];
    private double[] resistance_reading_ok = new double[28];
    private double[] load_is_on = new double[28];
    private double[] voltage_sensor_fault = new double[28];
    private double[] temperature_sensor_fault = new double[28];
    private double[] resistance_calculation_fault = new double[28];
    private double[] load_fault = new double[28];

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
                double fault_state  = (int) data[0] & 0b00000001;
                double K1_contactor = ((int) data[0] & 0b00000010) >> 1;
                double K2_contactor = ((int) data[0] & 0b00000100) >> 2;
                double K3_contactor = ((int) data[0] & 0b00001000) >> 3;
                double relay_fault = ((int) data[0] & 0b00010000) >> 4;

                double powerup_time = (((int) data[1] << 8) & (int) data[2]); // [s]

                // Byte of flags
                double power_from_source = (int) data[3] & 0b00000001;
                double power_from_load = ((int) data[3] & 0b00000010) >> 1;
                double interlock_tripped = ((int) data[3] & 0b00000100) >> 2;
                double hard_wire_contactor_request = ((int) data[3] & 0b00001000) >> 3;
                double can_contactor_request = ((int) data[3] & 0b00010000) >> 4;
                double HLIM_set = ((int) data[3] & 0b00100000) >> 5;
                double LLIM_set = ((int) data[3] & 0b01000000) >> 6;
                double fan_on = ((int) data[3] & 0b10000000) >> 7;

                // Fault code, stored
                double fault_code = (int) data[4];

                // Level fault flags
                double driving_off_while_plugged_in = (int) data[5] & 0b00000001;
                double interlock_tripped2 = ((int) data[5] & 0b00000010) >> 1;
                double comm_fault_blank_or_cell = ((int) data[5] & 0b00000100) >> 2;
                double charge_overcurrent = ((int) data[5] & 0b00001000) >> 3;
                double discharge_overcurrent = ((int) data[5] & 0b00010000) >> 4;
                double over_temp = ((int) data[5] & 0b00100000) >> 5;
                double under_voltage = ((int) data[5] & 0b01000000) >> 6;
                double over_voltage = ((int) data[5] & 0b10000000) >> 7;

                // Warnings flags
                double low_voltage = (int) data[6] & 0b00000001;
                double high_voltage = ((int) data[6] & 0b00000010) >> 1;
                double charge_overcurrent_warning = ((int) data[6] & 0b00000100) >> 2;
                double discharge_overcurrent_warning = ((int) data[6] & 0b00001000) >> 3;
                double cold_temp = ((int) data[6] & 0b00010000) >> 4;
                double hot_temp = ((int) data[6] & 0b00100000) >> 5;
                double low_SOH = ((int) data[6] & 0b01000000) >> 6;
                double isolateion_fault = ((int) data[6] & 0b10000000) >> 7;

            case "623":
                double pack_voltage = (((int) data[0] << 8) & (int) data[1]); // [V] [0,65535]
                double min_voltage = (int) data[2]/10.0; // [100mV] [0, 255] -> [V] [0.0,25.5]
                double min_voltage_id = (int) data[3]; // [0,255]
                double max_voltage = (int) data[4]/10.0; // [100mV] [0, 255] -> [V] [0.0,25.5]
                double max_voltage_id = (int) data[5]; // [0,255]

            case "624":
                double current = (((int) data[0] << 8) & (int) data[1]); // [A] signed! [-32764, 32764]
                double charge_limit = (((int) data[2] << 8) & (int) data[3]); // [A] [0, 65535]
                double discharge_limit = (((int) data[4] << 8) & (int) data[5]); // [A]

            case "625":
                double batt_energy_in  = ((int) data[0] << 8*3) & ((int) data[1] << 8*2) & ((int) data[2] << 8) & (int) data[3]; // [kWh][0,4294967295]
                double batt_energy_out = ((int) data[4] << 8*3) & ((int) data[5] << 8*2) & ((int) data[6] << 8) & (int) data[7]; // [kWh][0,4294967295]

            case "626":
                double SOC = (int) data[0]; // [%] [0,100]
                double DOD = (((int) data[1] << 8) & (int) data[2]); // [AH] [0,65535]
                double capacity = (((int) data[3] << 8) & (int) data[4]); // [AH] [0,65535]
                double SOH = (int) data[6]; // [%] [0,100]

            case "627":
                double temperature = (int) data[0]; // [C] signed! [-127,127]
                double min_temp = (int) data[2]; // [C] signed! [-127,127]
                double min_temp_id = (int) data[3];
                double max_temp = (int) data[4]; // [C] signed! [-127,127]
                double max_temp_id = (int) data[5];

            case "628":
                double pack_resistance = (((int) data[0] << 8) & (int) data[1])/10.0; // [100 micro-ohm][0,65525] -> [milli ohm] [0.0,6552.5]
                double min_resistance = ((int) data[2])/10.0; // [100 micro-ohm][0,255] -> [milli ohm] [0.0,25.5]
                double min_resistance_id = (int) data[3];
                double max_resistance = (int) data[4]; // [100 micro-ohm][0,255] -> [milli ohm] [0.0,25.5]
                double max_resistance_id = (int) data[5];

            default:
                int BASE_DUMP_ID = 0;
                int id = Integer.parseInt(msg[2]);

                // Individual cell details
                if (id == BASE_DUMP_ID){
                    this.voltages[data[0]] = (((int) data[1]) + 2.0)/100.0 ; // [10mV] [200, 455] -> [V] [2.00, 4.55]
                    this.temp_all_the_time[data[0]] = ((int) data[2]) - 128; // °C [-128, 127]
                    this.temp_when_balacing_off[data[0]] = ((int) data[3]) - 128; // °C [-128, 127]
                    this.resistance[data[0]] = ((int) data[4])/10.0; // [100 microOhm] [0,255] -> [miliOhm] [0.0, 25.5]
                    // Status
                    this.voltage_reading_ok[data[0]] = (int) data[5] & 0b00000001;
                    this.temperature_reading_ok[data[0]] = ((int) data[5] & 0b00000010) >> 1;
                    this.resistance_reading_ok[data[0]] = ((int) data[5] & 0b00000100) >> 2;
                    this.load_is_on[data[0]] = ((int) data[5] & 0b00001000) >> 3;
                    this.voltage_sensor_fault[data[0]] = ((int) data[5] & 0b00010000) >> 4;
                    this.temperature_sensor_fault[data[0]] = ((int) data[5] & 0b00100000) >> 5;
                    this.resistance_calculation_fault[data[0]] = ((int) data[5] & 0b01000000) >> 6;
                    this.load_fault[data[0]] = ((int) data[5] & 0b10000000) >> 7;
                }

                // Cell voltages
                else if (id > BASE_DUMP_ID && id <= (BASE_DUMP_ID+4)){ // +4 porque son hasta  28 módulos
                    this.voltages[((id-BASE_DUMP_ID-1)*8)  ] = (((int) data[0]) + 2.0)/100.0; // [10mV] [200, 455] -> [V] [2.00, 4.55]
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+1] = (((int) data[1]) + 2.0)/100.0;
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+2] = (((int) data[2]) + 2.0)/100.0;
                    this.voltages[((id-BASE_DUMP_ID-1)*8)+3] = (((int) data[3]) + 2.0)/100.0;
                    if(id != BASE_DUMP_ID+4) {
                        this.voltages[((id-BASE_DUMP_ID-1)*8)+4] = (((int) data[4]) + 2.0)/100.0;
                        this.voltages[((id - BASE_DUMP_ID - 1) * 8) + 5] = (((int) data[5]) + 2.0) / 100.0;
                        this.voltages[((id - BASE_DUMP_ID - 1) * 8) + 6] = (((int) data[6]) + 2.0) / 100.0;
                        this.voltages[((id - BASE_DUMP_ID - 1) * 8) + 7] = (((int) data[7]) + 2.0) / 100.0;
                    }
                }

        }
        System.out.println("ID: " + msg[2] + " MSG: ");
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
