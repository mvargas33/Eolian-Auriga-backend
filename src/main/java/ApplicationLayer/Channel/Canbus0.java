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

    private AppComponent sevcon;
    private final int lenSevcon = 14; // Hardcoded, specific, actual values updated in this implementation for this Component

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
            this.sevcon = this.myComponentsMap.get("sevcon"); // Must match name in .xlsx file
            if(sevcon != null){
                int len = sevcon.len;
                if(len != this.lenSevcon){
                    throw new Exception("Cantidad de valores del SEVCON en AppComponent != Cantidad de valores de lectura implementados");
                }
            }else{
                throw new Exception("A Sevcon AppComponent was not supplied in Canbus1 channel");
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
        processBuilder.command("bash", "-c", "python3 /home/pi/Desktop/lecturas/COdigo_rendimiento.py");
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
        this.sevcon.valoresRealesActuales[13] = 0.3*2*3.6*3.1416*this.sevcon.valoresRealesActuales[4]/60;
        switch (msg[0].split(":")[1]){
            case "100":
                this.sevcon.valoresRealesActuales[0] = Double.parseDouble(msg[2].split(":")[1]);//v bat
                this.sevcon.valoresRealesActuales[1] = Double.parseDouble(msg[1].split(":")[1]); // current bat
                this.sevcon.valoresRealesActuales[2] = Double.parseDouble(msg[3].split(":")[1]); //temp inv
                this.sevcon.valoresRealesActuales[6] = Double.parseDouble(msg[4].split(":")[1]); // potin
                break;
            case "200":
                //'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+','+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)+'\n'
                this.sevcon.valoresRealesActuales[3] = Double.parseDouble(msg[2].split(":")[1]); // torque
                this.sevcon.valoresRealesActuales[4] = Double.parseDouble(msg[4].split(":")[1]); // rpm
                this.sevcon.valoresRealesActuales[5] = Double.parseDouble(msg[1].split(":")[1]);// corriente motor
                this.sevcon.valoresRealesActuales[7] = Double.parseDouble(msg[5].split(":")[1]); // potout
                break;
            case "300":
                this.sevcon.valoresRealesActuales[10] = Double.parseDouble(msg[3].split(":")[1]); // torque_act
                this.sevcon.valoresRealesActuales[11] = Double.parseDouble(msg[1].split(":")[1]); // target lq
                this.sevcon.valoresRealesActuales[12] = Double.parseDouble(msg[2].split(":")[1]); // lq
                break;
            case "400":
                this.sevcon.valoresRealesActuales[8] = Double.parseDouble(msg[1].split(":")[1]);  // acelerador volt
                this.sevcon.valoresRealesActuales[9] =  Double.parseDouble(msg[3].split(":")[1]); // freno_volt
                break;
            default:
                System.out.println("Trama "+msg[0]+" no procesada");
        } // switch
    } // parseMessage()
}
