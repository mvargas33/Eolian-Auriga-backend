package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.Utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CanbusKelly extends Channel {
    private int[] data = new int[8]; // Memory efficient buffer

    private AppComponent sevcon;
    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public CanbusKelly(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);
        try{
            this.sevcon = this.myComponentsMap.get("sevcon"); // Must match name in .xlsx file
            if(sevcon == null){
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
        processBuilder.command("bash", "-c", "python3 /home/pi/Desktop/Lectura_kelly.py");
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
        stringBuilder.append("Iniciando lecturas del kelly...");
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
        if(message.charAt(0) == 'N') return;
        //String[] msg = Utils.split(message, " "); // Better performance split than String.split()
        String[] msg = message.split(","); // etter performance split than String.split()

        // if (msg.length != 16){ // If it isn't CAN-type message
        //     System.out.println("Message is not CAN-type. Split length is not 16.");
        //     System.out.println(message);
        //     return;
        // }

        // Parse HEX strings to byte data type, into local buffer
        switch (msg[0].split(":")[1]){
            case "freno_v":
                this.sevcon.valoresRealesActuales[9] = Double.parseDouble(msg[0].split(":")[1]);//v bat
                this.sevcon.valoresRealesActuales[8] = Double.parseDouble(msg[1].split(":")[1]);//v bat
                this.sevcon.valoresRealesActuales[0] = Double.parseDouble(msg[2].split(":")[1]);//v bat
                break;
            case "bat_c":
                //'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+','+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)+'\n'
                this.sevcon.valoresRealesActuales[5] = Double.parseDouble(msg[0].split(":")[1]); // torque
                
                // this.lcd.valoresRealesActuales[0] = this.sevcon.valoresRealesActuales[7]; // pot
                // this.lcd.valoresRealesActuales[1] = this.sevcon.valoresRealesActuales[3]; // torque
                // this.lcd.valoresRealesActuales[2] = this.sevcon.valoresRealesActuales[5]; // corriente
                // this.lcd.valoresRealesActuales[3] = this.sevcon.valoresRealesActuales[13]; // velocidad
                break;
            // case "motor_temp":
            //     this.sevcon.valoresRealesActuales[2] = Double.parseDouble(msg[0].split(":")[1]); // torque_act
            //     break;
            case "rpm":
                this.sevcon.valoresRealesActuales[1] = Double.parseDouble(msg[0].split(":")[1]);
                this.sevcon.valoresRealesActuales[2] = Double.parseDouble(msg[1].split(":")[1]);
                break;
            default:
                System.out.println("Trama "+msg[0]+" no procesada");
        } // switch
    } // parseMessage()
}
