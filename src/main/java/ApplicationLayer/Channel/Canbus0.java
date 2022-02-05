package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.Utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Canbus0 extends Channel {
    private int[] data = new int[8]; // Memory efficient buffer

    private AppComponent sevcon_izq;
    private AppComponent sevcon_der;
    private AppComponent lcd;
    private final int lenSevcon = 16; // Hardcoded, specific, actual values updated in this implementation for this Component

    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public Canbus0(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices, new String[] {"BMS", "MPPT"});
        // Check that a BMS AppComponent was supplied
        // With the exact amount of double[] values as the implementation here
        for(AppComponent ac : myComponentList) {
            if(ac.getID().equals("sevcon_izq")) {
                sevcon_izq = ac;
            }
            else if(ac.getID().equals("sevcon_der")) {
                sevcon_der = ac;
            }
            else if(ac.getID().equals("lcd")) {
                lcd = ac;
            }
        }
        // try{
        //     this.sevcon = this.myComponentsMap.get("sevcon"); // Must match name in .xlsx file
        //     if(sevcon != null){
        //         int len = sevcon.len;
        //         if(len != this.lenSevcon){
        //             throw new Exception("Cantidad de valores del SEVCON en AppComponent != Cantidad de valores de lectura implementados");
        //         }
        //     }else{
        //         throw new Exception("A Sevcon AppComponent was not supplied in Canbus1 channel");
        //     }
        // }catch(Exception e){
        //     e.printStackTrace();
        // }
        //this.lcd = this.myComponentsMap.get("lcd");
    }

    /**
     * Main reading and parsing loop
     */
    @Override
    public void readingLoop() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "python3 /home/pi/Desktop/lectura/Codigo_rendimiento.py");
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
        stringBuilder.append("Iniciando lecturas del sevcon...");
        //stringBuilder.append("cd ./src/main/java/ApplicationLayer/SensorReading/CANReaders/linux-can-utils;");
        //stringBuilder.append("gcc candump.c lib.c -o candump;"); // Comment this on second execution, no need to recompile
        processBuilder.command("bash", "-c", stringBuilder.toString());
        // try {
        //     processBuilder.start();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    /**
     * Parsing function. Transforms CANBUS message from console to double,
     * into AppComponent bms's double[] valoresRealesActuales, directly.
     * @param message
     */
    public void parseMessage(String message) {
        //String[] msg = Utils.split(message, " "); // Better performance split than String.split()
        String[] msg = message.split(","); // etter performance split than String.split()

        // if (msg.length != 16){ // If it isn't CAN-type message
        //     System.out.println("Message is not CAN-type. Split length is not 16.");
        //     System.out.println(message);
        //     return;
        // }

        // Parse HEX strings to byte data type, into local buffer
        switch (msg[0].split(":")[1]){
            case "100":
                this.sevcon_izq.valoresRealesActuales[0] = Double.parseDouble(msg[2].split(":")[1]);//v bat
                this.sevcon_izq.valoresRealesActuales[1] = Double.parseDouble(msg[1].split(":")[1]); // current bat
                this.sevcon_izq.valoresRealesActuales[2] = Double.parseDouble(msg[3].split(":")[1]); //temp inv
                this.sevcon_izq.valoresRealesActuales[6] = Double.parseDouble(msg[4].split(":")[1]); // potin
                break;
            case "200":
                //'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+','+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)+'\n'
                this.sevcon_izq.valoresRealesActuales[3] = Double.parseDouble(msg[2].split(":")[1]); // torque
                this.sevcon_izq.valoresRealesActuales[4] = Double.parseDouble(msg[4].split(":")[1]); // rpm
                this.sevcon_izq.valoresRealesActuales[5] = Double.parseDouble(msg[1].split(":")[1]);// corriente motor
                this.sevcon_izq.valoresRealesActuales[7] = Double.parseDouble(msg[5].split(":")[1]); // potout
                this.sevcon_izq.valoresRealesActuales[13] = Double.parseDouble(msg[3].split(":")[1]); // velocidad
                
                // this.lcd.valoresRealesActuales[0] = this.sevcon_izq.valoresRealesActuales[7]; // pot
                // this.lcd.valoresRealesActuales[1] = this.sevcon_izq.valoresRealesActuales[3]; // torque
                // this.lcd.valoresRealesActuales[2] = this.sevcon_izq.valoresRealesActuales[5]; // corriente
                // this.lcd.valoresRealesActuales[3] = this.sevcon_izq.valoresRealesActuales[13]; // velocidad
                break;
            case "300":
                this.sevcon_izq.valoresRealesActuales[10] = Double.parseDouble(msg[3].split(":")[1]); // torque_act
                this.sevcon_izq.valoresRealesActuales[11] = Double.parseDouble(msg[1].split(":")[1]); // target lq
                this.sevcon_izq.valoresRealesActuales[15] = Double.parseDouble(msg[4].split(":")[1]); // target lq_hex
                this.sevcon_izq.valoresRealesActuales[12] = Double.parseDouble(msg[2].split(":")[1]); // lq
                this.sevcon_izq.valoresRealesActuales[14] = Double.parseDouble(msg[5].split(":")[1]); // lq_hex
                break;
            case "400":
                this.sevcon_izq.valoresRealesActuales[8] = Double.parseDouble(msg[1].split(":")[1]);  // acelerador volt
                this.sevcon_izq.valoresRealesActuales[9] =  Double.parseDouble(msg[3].split(":")[1]); // freno_volt
                break;
            case "101":
                this.sevcon_der.valoresRealesActuales[0] = Double.parseDouble(msg[2].split(":")[1]);//v bat
                this.sevcon_der.valoresRealesActuales[1] = Double.parseDouble(msg[1].split(":")[1]); // current bat
                this.sevcon_der.valoresRealesActuales[2] = Double.parseDouble(msg[3].split(":")[1]); //temp inv
                this.sevcon_der.valoresRealesActuales[6] = Double.parseDouble(msg[4].split(":")[1]); // potin
                break;
            case "201":
                //'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+','+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)+'\n'
                this.sevcon_der.valoresRealesActuales[3] = Double.parseDouble(msg[2].split(":")[1]); // torque
                this.sevcon_der.valoresRealesActuales[4] = Double.parseDouble(msg[4].split(":")[1]); // rpm
                this.sevcon_der.valoresRealesActuales[5] = Double.parseDouble(msg[1].split(":")[1]);// corriente motor
                this.sevcon_der.valoresRealesActuales[7] = Double.parseDouble(msg[5].split(":")[1]); // potout
                this.sevcon_der.valoresRealesActuales[13] = Double.parseDouble(msg[3].split(":")[1]); // velocidad
                
                // this.lcd.valoresRealesActuales[0] = this.sevcon_der.valoresRealesActuales[7]; // pot
                // this.lcd.valoresRealesActuales[1] = this.sevcon_der.valoresRealesActuales[3]; // torque
                // this.lcd.valoresRealesActuales[2] = this.sevcon_der.valoresRealesActuales[5]; // corriente
                // this.lcd.valoresRealesActuales[3] = this.sevcon_der.valoresRealesActuales[13]; // velocidad
                break;
            case "301":
                this.sevcon_der.valoresRealesActuales[10] = Double.parseDouble(msg[3].split(":")[1]); // torque_act
                this.sevcon_der.valoresRealesActuales[11] = Double.parseDouble(msg[1].split(":")[1]); // target lq
                this.sevcon_der.valoresRealesActuales[15] = Double.parseDouble(msg[4].split(":")[1]); // target lq_hex
                this.sevcon_der.valoresRealesActuales[12] = Double.parseDouble(msg[2].split(":")[1]); // lq
                this.sevcon_der.valoresRealesActuales[14] = Double.parseDouble(msg[5].split(":")[1]); // lq_hex
                break;
            case "401":
                this.sevcon_der.valoresRealesActuales[8] = Double.parseDouble(msg[1].split(":")[1]);  // acelerador volt
                this.sevcon_der.valoresRealesActuales[9] =  Double.parseDouble(msg[3].split(":")[1]); // freno_volt
                break;
            default:
                System.out.println("Trama "+msg[0]+" no procesada");
        } // switch
    } // parseMessage()
}
