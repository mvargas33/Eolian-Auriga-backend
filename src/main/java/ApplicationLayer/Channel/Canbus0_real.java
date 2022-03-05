package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Canbus0_real extends Channel {
    private int[] data = new int[8]; // Memory efficient buffer

    private AppComponent sevcon;
    private final int lenSEVCON = 32; // Hardcoded, specific, actual values updated in this implementation for this Component

    private final int message_100_index = 3; // 3 data
    private final int message_200_index = 0; // 3 data
    private final int message_300_index = 6; // 4 data
    private final int message_400_index = 10; // 4 data
    private final int message_102_index = 19; // 3
    private final int message_202_index = 16; // 3
    private final int message_302_index = 22; // 4
    private final int message_402_index = 26; // 4

    private boolean dev;

    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public Canbus0_real(List<AppComponent> myComponentList, List<Service> myServices, boolean dev) {
        super(myComponentList, myServices);
        this.dev = dev;
        // Check that a BMS AppComponent was supplied
        // With the exact amount of double[] values as the implementation here
        try{
            this.sevcon = this.myComponentsMap.get("sevcon"); // Must match name in .xlsx file
            if(sevcon != null){
                int len = sevcon.len;
                if(len != this.lenSEVCON){
                    throw new Exception("Cantidad de valores de SEVCON en AppComponent != Cantidad de valores de lectura implementados");
                }
            }else{
                throw new Exception("A Sevcon AppComponent was not supplied in Canbus0 channel");
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
        if(dev) {
            processBuilder.command("bash", "-c", "candump vcan0");
        }
        else {
            processBuilder.command("bash", "-c", "candump can0");
        }
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

    // msb indexado desde 0 a 31
    public int twoComp(int val, int msb) {
        if(msb == 31) {
            return val;
        }
        else {
            if(((val >> msb) & 1) == 1) {
                return -(1<<(msb+1))+val;
            }
            else {
                return val;
            }
        }
    }

    /**
     * Commands executed once
     */
    @Override
    public void setUp() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        //processBuilder.redirectErrorStream(true);
        // NOTA: primero hay que iniciar el can com en comando 'stty -F /dev/serial0 raw 9600 cs8 clocal -cstopb'
        // (9600 es el baud rate)
        
        StringBuilder stringBuilder = new StringBuilder();
        if(dev) {
            stringBuilder.append("sudo /sbin/ip link add dev vcan0 type vcan;sudo /sbin/ip link set vcan0 up");
        }
        else {
            stringBuilder.append("sudo /sbin/ip link set can0 up type can bitrate 1000000");
        }
        //stringBuilder.append("cd ./src/main/java/ApplicationLayer/SensorReading/CANReaders/linux-can-utils;");
        //stringBuilder.append("gcc candump.c lib.c -o candump;"); // Comment this on second execution, no need to recompile
        processBuilder.command("bash", "-c", stringBuilder.toString());
        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public int parseCan(int[] data, int start, int end) {
        int o = 0;
        int offset = 0;
        for(int i = start; i <= end; i++) {
            o = o | (data[i] << offset);
            offset += 8;
        }
        return twoComp(o, (end-start+1)*8-1);
    }

    /**
     * Parsing function. Transforms CANBUS message from console to double,
     * into AppComponent bms's double[] valoresRealesActuales, directly.
     * @param message
     */
    public void parseMessage(String message) {
        //String[] msg = Utils.split(message, " "); // Better performance split than String.split()
        //System.out.println(message);
        String[] msg = message.split("\\s+"); // Better performance split than String.split()

        // if (msg.length != 16){ // If it isn't CAN-type message
        //     System.out.println("Message is not CAN-type. Split length is not 16.");
        //     System.out.println(message);
        //     return;
        // }

        // Parse HEX strings to byte data type, into local buffer
        int L = Character.getNumericValue(msg[3].charAt(1));
        for(int i=0 ; i<L; i++){ //asume mensaje can de 8 bytes fijo, todo: hacer mas flexible en el futuro.
            // atento a esto en la prueba, puede estar alrevez
            data[i] = Integer.parseInt(msg[4+i], 16);
        }

        switch (msg[2]){
            case "100":
                this.sevcon.valoresRealesActuales[message_100_index    ] = 0.0625 * parseCan(data, 0, 1); // der_battery_V
                this.sevcon.valoresRealesActuales[message_100_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3); // der_battery_I SIGNED
                this.sevcon.valoresRealesActuales[message_100_index + 2] = parseCan(data, 4, 4); // der_inverter_temp
                break;
            case "200":
                this.sevcon.valoresRealesActuales[message_200_index    ] = -1 * parseCan(data, 0, 1); // der_motor_I SIGNED [A]
                this.sevcon.valoresRealesActuales[message_200_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3) ; // der_motor_torque_demand SIGNED
                this.sevcon.valoresRealesActuales[message_200_index + 2] = -1 * parseCan(data, 4, 7) ; // der_motor_RPM SIGNED [rad/s]
                break;
            case "300":
                this.sevcon.valoresRealesActuales[message_300_index    ] = -1 * 0.0625 * parseCan(data, 0, 1); // der_target_I_quadrature SIGNED
                this.sevcon.valoresRealesActuales[message_300_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3); // der_I_quadrature SIGNED
                this.sevcon.valoresRealesActuales[message_300_index + 2] = -1 * 0.0625 * parseCan(data, 4, 5); // der_torque_actual SIGNED
                this.sevcon.valoresRealesActuales[message_300_index + 3] = -1 * 0.0625 * parseCan(data, 6, 7); // der_V_quadrature SIGNED
                break;
            case "400":
                this.sevcon.valoresRealesActuales[message_400_index    ] = 0.00390625 *  parseCan(data, 0, 1); // der_throttle_V [V]
                this.sevcon.valoresRealesActuales[message_400_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3); // der_target_I_direct SIGNED
                this.sevcon.valoresRealesActuales[message_400_index + 2] = -1 * 0.0625 * parseCan(data, 4, 5); // der_I_direct SIGNED
                this.sevcon.valoresRealesActuales[message_400_index + 3] = -1 * 0.0625 * parseCan(data, 6, 7); // der_V_direct SIGNED
                break;
            case "102":
                this.sevcon.valoresRealesActuales[message_102_index    ] = 0.0625 * parseCan(data, 0, 1); // izq_battery_V
                this.sevcon.valoresRealesActuales[message_102_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3); // izq_battery_I SIGNED
                this.sevcon.valoresRealesActuales[message_102_index + 2] = parseCan(data, 4, 4); // izq_inverter_temp
                break;
            case "202":
                this.sevcon.valoresRealesActuales[message_202_index    ] = -1 * parseCan(data, 0, 1) ; // izq_motor_I SIGNED
                this.sevcon.valoresRealesActuales[message_202_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3); // izq_motor_torque_demand SIGNED
                this.sevcon.valoresRealesActuales[message_202_index + 2] = -1 * parseCan(data, 4, 7); // izq_motor_RPM SIGNED
                break;
            case "302":
                this.sevcon.valoresRealesActuales[message_302_index    ] = -1 * 0.0625 * parseCan(data, 0, 1); // izq_target_I_quadrature SIGNED
                this.sevcon.valoresRealesActuales[message_302_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3); // izq_I_quadrature SIGNED
                this.sevcon.valoresRealesActuales[message_302_index + 2] = -1 * 0.0625    * parseCan(data, 4, 5); // izq_torque_actual SIGNED
                this.sevcon.valoresRealesActuales[message_302_index + 3] = -1 * 0.0625 * parseCan(data, 6, 7); // izq_V_quadrature SIGNED
                break;
            case "402":
                this.sevcon.valoresRealesActuales[message_402_index    ] = 0.00390625 *  parseCan(data, 0, 1);  // izq_throttle_V [V]
                this.sevcon.valoresRealesActuales[message_402_index + 1] = -1 * 0.0625 * parseCan(data, 2, 3);  // izq_target_I_direct SIGNED
                this.sevcon.valoresRealesActuales[message_402_index + 2] = -1 * 0.0625 * parseCan(data, 4, 5);  // izq_I_direct SIGNED
                this.sevcon.valoresRealesActuales[message_402_index + 3] = -1 * 0.0625 * parseCan(data, 6, 7);  // izq_V_direct SIGNED
                break;
            default:
                break;
                //System.out.print("ID: " + msg[2] + " MSG: ");
                //for(int i=0 ; i< L; i++){
                //    System.out.print(" " + msg[i+4]);
                //}System.out.println("");
            // for(int i = 0; i < sevcon.valoresRealesActuales.length; i++) {
            //     System.out.println(sevcon.nombreParametros[i] + ": "+sevcon.valoresRealesActuales[i]);
            // }
        } // switch
    } // parseMessage()
}
