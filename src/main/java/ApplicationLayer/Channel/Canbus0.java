package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.Utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Canbus0 extends Channel {
    private byte[] data = new byte[8]; // Memory efficient buffer

    private AppComponent bms;
    private final int lenBMS = 391; // Hardcoded, specific, actual values updated in this implementation for this Component

    private final int message_622_index = 0; // 31 data
    private final int message_623_index = 31; // 5 data
    private final int message_624_index = 36; // 3
    private final int message_625_index = 39; // 2
    private final int message_626_index = 41; // 4
    private final int message_627_index = 45; // 5
    private final int message_628_index = 50; // 5
    private final int voltages_index = 55;                        // 28 cells
    private final int temp_all_the_time_index = 83;               // 28 cells
    private final int temp_when_balacing_off_index = 111;         // 28 cells
    private final int resistance_index = 139;                     // 28 cells
    private final int voltage_reading_ok_index = 167;             // 28 cells
    private final int temperature_reading_ok_index = 195;         // 28 cells
    private final int resistance_reading_ok_index = 223;          // 28 cells
    private final int load_is_on_index = 251;                     // 28 cells
    private final int voltage_sensor_fault_index = 279;           // 28 cells
    private final int temperature_sensor_fault_index = 307;       // 28 cells
    private final int resistance_calculation_fault_index = 335;   // 28 cells
    private final int load_fault_index = 363;                     // 28 cells

    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public Canbus0(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);
        // Check that a BMS AppComponent was supplied
        // With the exact amount of double[] values as the implementation here
        try{
            this.bms = this.myComponentsMap.get("bms"); // Must match name in .xlsx file
            if(bms != null){
                int len = bms.len;
                if(len != this.lenBMS){
                    throw new Exception("Cantidad de valores de BMS en AppComponent != Cantidad de valores de lectura implementados");
                }
            }else{
                throw new Exception("A BMS AppComponent was not supplied in Canbus0 channel");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Main reading and parsing loop
     */
    @Override
    public void readingLoop() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "candump any;");
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while(true){
                try{
                    while ((line = reader.readLine()) != null) {
                        parseMessage(line);
                        super.informServices(); // Call this just after all AppComponent in myComponentList were updated
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Commands executed once
     */
    @Override
    public void setUp() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        // NOTA: primero hay que iniciar el can com en comando 'stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb'
        // (9600 es el baud rate)
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sudo /sbin/ip link set can0 up type can bitrate 500000");
        //stringBuilder.append("cd ./src/main/java/ApplicationLayer/SensorReading/CANReaders/linux-can-utils;");
        //stringBuilder.append("gcc candump.c lib.c -o candump;"); // Comment this on second execution, no need to recompile
        processBuilder.command("bash", "-c", stringBuilder.toString());
        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parsing function. Transforms CANBUS message from console to double,
     * into AppComponent bms's double[] valoresRealesActuales, directly.
     * @param message
     */
    void parseMessage(String message) {
        String[] msg = Utils.split(message, " "); // Better performance split than String.split()

        // if (msg.length != 16){ // If it isn't CAN-type message
        //     System.out.println("Message is not CAN-type. Split length is not 16.");
        //     System.out.println(message);
        //     return;
        // }

        // Parse HEX strings to byte data type, into local buffer
        int L = Character.getNumericValue(msg[2].charAt(1));
        for(int i=0 ; i<L; i++){ //asume mensaje can de 8 bytes fijo, todo: hacer mas flexible en el futuro.
            // atento a esto en la prueba, puede estar alrevez
            data[i] = (byte) ((Character.digit(msg[3+i].charAt(0), 16) << 4) + (Character.digit(msg[3+i].charAt(1), 16)));
        }

        switch (msg[1]){
            case "502":
                // State of system
                this.bms.valoresRealesActuales[message_622_index    ]  = (int) data[0] & 0b00000001;        // fault_state
                this.bms.valoresRealesActuales[message_622_index + 1] = ((int) data[0] & 0b00000010) >> 1;  // K1_contactor
                this.bms.valoresRealesActuales[message_622_index + 2] = ((int) data[0] & 0b00000100) >> 2;  // K2_contactor
                this.bms.valoresRealesActuales[message_622_index + 3] = ((int) data[0] & 0b00001000) >> 3;  // K3_contactor
                this.bms.valoresRealesActuales[message_622_index + 4] = ((int) data[0] & 0b00010000) >> 4;  // relay_fault

                this.bms.valoresRealesActuales[message_622_index + 5] = (((int) data[1] << 8) & (int) data[2]); // powerup_time [s]

                // Byte of flags
                this.bms.valoresRealesActuales[message_622_index + 6] = (int) data[3] & 0b00000001;         // power_from_source
                this.bms.valoresRealesActuales[message_622_index + 7] = ((int) data[3] & 0b00000010) >> 1;  // power_from_load
                this.bms.valoresRealesActuales[message_622_index + 8] = ((int) data[3] & 0b00000100) >> 2;  // interlock_tripped
                this.bms.valoresRealesActuales[message_622_index + 9] = ((int) data[3] & 0b00001000) >> 3;  // hard_wire_contactor_request
                this.bms.valoresRealesActuales[message_622_index + 10] = ((int) data[3] & 0b00010000) >> 4; // can_contactor_request
                this.bms.valoresRealesActuales[message_622_index + 11] = ((int) data[3] & 0b00100000) >> 5; // HLIM_set
                this.bms.valoresRealesActuales[message_622_index + 12] = ((int) data[3] & 0b01000000) >> 6; // LLIM_set
                this.bms.valoresRealesActuales[message_622_index + 13] = ((int) data[3] & 0b10000000) >> 7; // fan_on

                // Fault code, stored
                this.bms.valoresRealesActuales[message_622_index + 14] = (int) data[4];  // fault_code

                // Level fault flags
                this.bms.valoresRealesActuales[message_622_index + 15] = (int) data[5] & 0b00000001;        // driving_off_while_plugged_in
                this.bms.valoresRealesActuales[message_622_index + 16] = ((int) data[5] & 0b00000010) >> 1; // interlock_tripped2
                this.bms.valoresRealesActuales[message_622_index + 17] = ((int) data[5] & 0b00000100) >> 2; // comm_fault_blank_or_cell
                this.bms.valoresRealesActuales[message_622_index + 18] = ((int) data[5] & 0b00001000) >> 3; // charge_overcurrent
                this.bms.valoresRealesActuales[message_622_index + 19] = ((int) data[5] & 0b00010000) >> 4; // discharge_overcurrent
                this.bms.valoresRealesActuales[message_622_index + 20] = ((int) data[5] & 0b00100000) >> 5; // over_temp
                this.bms.valoresRealesActuales[message_622_index + 21] = ((int) data[5] & 0b01000000) >> 6; // under_voltage
                this.bms.valoresRealesActuales[message_622_index + 22] = ((int) data[5] & 0b10000000) >> 7; // over_voltage

                // Warnings flags
                this.bms.valoresRealesActuales[message_622_index + 23] = (int) data[6] & 0b00000001;        // low_voltage
                this.bms.valoresRealesActuales[message_622_index + 24] = ((int) data[6] & 0b00000010) >> 1; // high_voltage
                this.bms.valoresRealesActuales[message_622_index + 25] = ((int) data[6] & 0b00000100) >> 2; // charge_overcurrent_warning
                this.bms.valoresRealesActuales[message_622_index + 26] = ((int) data[6] & 0b00001000) >> 3; // discharge_overcurrent_warning
                this.bms.valoresRealesActuales[message_622_index + 27] = ((int) data[6] & 0b00010000) >> 4; // cold_temp
                this.bms.valoresRealesActuales[message_622_index + 28] = ((int) data[6] & 0b00100000) >> 5; // hot_temp
                this.bms.valoresRealesActuales[message_622_index + 29] = ((int) data[6] & 0b01000000) >> 6; // low_SOH
                this.bms.valoresRealesActuales[message_622_index + 30] = ((int) data[6] & 0b10000000) >> 7; // isolateion_fault

            case "503":
                this.bms.valoresRealesActuales[message_623_index    ] = (((int) data[0] << 8) & (int) data[1]); // pack_voltage   [V]       [0,65535]
                this.bms.valoresRealesActuales[message_623_index + 1] = (int) data[2]/10.0;                     // min_voltage    [100mV]   [0, 255] -> [V] [0.0,25.5]
                this.bms.valoresRealesActuales[message_623_index + 2] = (int) data[3];                          // min_voltage_id           [0,255]
                this.bms.valoresRealesActuales[message_623_index + 3] = (int) data[4]/10.0;                     // max_voltage    [100mV]   [0, 255] -> [V] [0.0,25.5]
                this.bms.valoresRealesActuales[message_623_index + 4] = (int) data[5];                          // max_voltage_id [0,255]

            case "504":
                this.bms.valoresRealesActuales[message_624_index    ] = (((int) data[0] << 8) & (int) data[1]); // current          [A] signed! [-32764, 32764]
                this.bms.valoresRealesActuales[message_624_index + 1] = (((int) data[2] << 8) & (int) data[3]); // charge_limit     [A]         [0, 65535]
                this.bms.valoresRealesActuales[message_624_index + 2] = (((int) data[4] << 8) & (int) data[5]); // discharge_limit  [A]

            case "505":
                this.bms.valoresRealesActuales[message_625_index    ] = ((int) data[0] << 8*3) & ((int) data[1] << 8*2) & ((int) data[2] << 8) & (int) data[3]; // batt_energy_in  [kWh][0,4294967295]
                this.bms.valoresRealesActuales[message_625_index + 1] = ((int) data[4] << 8*3) & ((int) data[5] << 8*2) & ((int) data[6] << 8) & (int) data[7]; // batt_energy_out [kWh][0,4294967295]

            case "506":
                this.bms.valoresRealesActuales[message_626_index    ] = (int) data[0];                          // SOC      [%]  [0,100]
                this.bms.valoresRealesActuales[message_626_index + 1] = (((int) data[1] << 8) & (int) data[2]); // DOD      [AH] [0,65535]
                this.bms.valoresRealesActuales[message_626_index + 2] = (((int) data[3] << 8) & (int) data[4]); // capacity [AH] [0,65535]
                this.bms.valoresRealesActuales[message_626_index + 3] = (int) data[6];                          // SOH      [%]  [0,100]

            case "507":
                this.bms.valoresRealesActuales[message_627_index    ] = (int) data[0]; // temperature [C] signed! [-127,127]
                this.bms.valoresRealesActuales[message_627_index + 1] = (int) data[2]; // min_temp    [C] signed! [-127,127]
                this.bms.valoresRealesActuales[message_627_index + 2] = (int) data[3]; // min_temp_id
                this.bms.valoresRealesActuales[message_627_index + 3] = (int) data[4]; // max_temp    [C] signed! [-127,127]
                this.bms.valoresRealesActuales[message_627_index + 4] = (int) data[5]; // max_temp_id

            case "508":
                this.bms.valoresRealesActuales[message_628_index    ] = (((int) data[0] << 8) & (int) data[1])/10.0;    // pack_resistance    [100 micro-ohm][0,65525] -> [milli ohm] [0.0,6552.5]
                this.bms.valoresRealesActuales[message_628_index + 1] = ((int) data[2])/10.0;                           // min_resistance     [100 micro-ohm][0,255]   -> [milli ohm] [0.0,25.5]
                this.bms.valoresRealesActuales[message_628_index + 2] = (int) data[3];                                  // min_resistance_id
                this.bms.valoresRealesActuales[message_628_index + 3] = (int) data[4];                                  // max_resistance     [100 micro-ohm][0,255]   -> [milli ohm] [0.0,25.5]
                this.bms.valoresRealesActuales[message_628_index + 4] = (int) data[5];                                  // max_resistance_id

            default:
                int BASE_DUMP_ID = Integer.parseInt("200", 16);
                int id = Integer.parseInt(msg[1], 16);
                // Individual cell details // data[0] = Cell ID [0-27]
                if (id == BASE_DUMP_ID){
                    if (data[0] > 27){
                        System.out.println("Message from cell outside range, ignoring");
                        System.out.print("ID: " + msg[1] + " MSG: ");
                        for(int i=0 ; i< L; i++){
                            System.out.print(" " + msg[i+3]);
                        }System.out.println("");
                        return;
                    }
                    this.bms.valoresRealesActuales[voltages_index + data[0]] = (((int) data[1]) + 2.0)/100.0 ; // voltages [10mV] [200, 455] -> [V] [2.00, 4.55]
                    this.bms.valoresRealesActuales[temp_all_the_time_index + data[0]] = ((int) data[2]) - 128; // temp_all_the_time °C [-128, 127]
                    this.bms.valoresRealesActuales[temp_when_balacing_off_index + data[0]] = ((int) data[3]) - 128; // temp_when_balacing_off °C [-128, 127]
                    this.bms.valoresRealesActuales[resistance_index + data[0]] = ((int) data[4])/10.0; // resistance [100 microOhm] [0,255] -> [miliOhm] [0.0, 25.5]
                    // Status
                    this.bms.valoresRealesActuales[voltage_reading_ok_index + data[0]] = (int) data[5] & 0b00000001; // voltage_reading_ok
                    this.bms.valoresRealesActuales[temperature_reading_ok_index + data[0]] = ((int) data[5] & 0b00000010) >> 1; // temperature_reading_ok
                    this.bms.valoresRealesActuales[resistance_reading_ok_index + data[0]] = ((int) data[5] & 0b00000100) >> 2; // resistance_reading_ok
                    this.bms.valoresRealesActuales[load_is_on_index + data[0]] = ((int) data[5] & 0b00001000) >> 3; // load_is_on
                    this.bms.valoresRealesActuales[voltage_sensor_fault_index + data[0]] = ((int) data[5] & 0b00010000) >> 4; // voltage_sensor_fault
                    this.bms.valoresRealesActuales[temperature_sensor_fault_index + data[0]] = ((int) data[5] & 0b00100000) >> 5; // temperature_sensor_fault
                    this.bms.valoresRealesActuales[resistance_calculation_fault_index + data[0]] = ((int) data[5] & 0b01000000) >> 6; // resistance_calculation_fault
                    this.bms.valoresRealesActuales[load_fault_index + data[0]] = ((int) data[5] & 0b10000000) >> 7; // load_fault
                }

                // Cell voltages
                else if (id > BASE_DUMP_ID && id <= (BASE_DUMP_ID+4)){ // +4 porque son hasta  28 módulos
                    this.bms.valoresRealesActuales[voltages_index + ((id-BASE_DUMP_ID-1)*8)  ] = (((int) data[0]) + 2.0)/100.0; // voltages [10mV] [200, 455] -> [V] [2.00, 4.55]
                    this.bms.valoresRealesActuales[voltages_index + ((id-BASE_DUMP_ID-1)*8)+1] = (((int) data[1]) + 2.0)/100.0;
                    this.bms.valoresRealesActuales[voltages_index + ((id-BASE_DUMP_ID-1)*8)+2] = (((int) data[2]) + 2.0)/100.0;
                    this.bms.valoresRealesActuales[voltages_index + ((id-BASE_DUMP_ID-1)*8)+3] = (((int) data[3]) + 2.0)/100.0;
                    if(id != BASE_DUMP_ID+4) { // Entra en 1-2-3, menos en el ultimo, ya que solo necesita rescatar los ultimos 4 modulos (3x8=24+4=28)
                        this.bms.valoresRealesActuales[voltages_index + ((id-BASE_DUMP_ID-1)*8)+4] = (((int) data[4]) + 2.0)/100.0;
                        this.bms.valoresRealesActuales[voltages_index + ((id - BASE_DUMP_ID - 1) * 8) + 5] = (((int) data[5]) + 2.0) / 100.0;
                        this.bms.valoresRealesActuales[voltages_index + ((id - BASE_DUMP_ID - 1) * 8) + 6] = (((int) data[6]) + 2.0) / 100.0;
                        this.bms.valoresRealesActuales[voltages_index + ((id - BASE_DUMP_ID - 1) * 8) + 7] = (((int) data[7]) + 2.0) / 100.0;
                    }
                }else{
                    System.out.print("ID: " + msg[1] + " MSG: ");
                    for(int i=0 ; i< L; i++){
                        System.out.print(" " + msg[i+3]);
                    }System.out.println("");
                }
        } // switch
    } // parseMessage()
}
