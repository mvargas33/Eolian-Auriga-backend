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

    private AppComponent kelly;
    private final int lenSevcon = 16; // Hardcoded, specific, actual values updated in this implementation for this Component
    private boolean dev = false;
    private boolean found = false;
    private int currentMsg = -1;
    private int lastLen = -1;
    private int currentKelly = -1; //0 para el izquierdo 1 para el derecho, -1 para aun no encontrado
    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public Canbus0(List<AppComponent> myComponentList, List<Service> myServices, boolean dev) {
        super(myComponentList, myServices);
        this.dev = dev;
        // Check that a BMS AppComponent was supplied
        // With the exact amount of double[] values as the implementation here
        for(AppComponent ac : myComponentList) {
            if(ac.getID().equals("kelly")) {
                kelly = ac;
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
        if(dev) {
            processBuilder.command("bash", "-c", "candump vcan0");
        }
        else {
            processBuilder.command("bash", "-c", "candump can0");
        }try {
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
        stringBuilder.append("sudo ip link set can0 up type can bitrate 1000000");
        //stringBuilder.append("cd ./src/main/java/ApplicationLayer/SensorReading/CANReaders/linux-can-utils;");
        //stringBuilder.append("gcc candump.c lib.c -o candump;"); // Comment this on second execution, no need to recompile
        processBuilder.command("bash", "-c", stringBuilder.toString());
        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int searchMsg(int lastLen, int L) {
        if(lastLen == 8 && L == 2) return 1;
        if(lastLen == 2 && L == 1) return 2;
        if(lastLen == 1 && L == 5) return 6;
        if(lastLen == 5 && L == 6) return 7;
        if(lastLen == 6 && L == 6) return 8;
        if(lastLen == 6 && L == 5) return 9;
        if(lastLen == 5 && L == 1) return 10;
        if(lastLen == 1 && L == 8) return 0;
        return -1;
    }

    /**
     * Parsing function. Transforms CANBUS message from console to double,
     * into AppComponent bms's double[] valoresRealesActuales, directly.
     * @param message
     */
    public void parseMessage(String message) {
        //String[] msg = Utils.split(message, " "); // Better performance split than String.split()
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

        // if (msg.length != 16){ // If it isn't CAN-type message
        //     System.out.println("Message is not CAN-type. Split length is not 16.");
        //     System.out.println(message);
        //     return;
        // }
        if(!found) {
            currentMsg = searchMsg(lastLen, L);
            if(currentMsg != -1) found = true;
            if(msg[2] == "0CD") currentKelly = 1;
            if(msg[2] == "069") currentKelly = 0;
        }
        // Ponerle las formulas de los kelly
        switch (currentMsg){
            case -1:
                break;
            case 0:
                kelly.valoresRealesActuales[currentKelly*13+0] = data[0]; // ejemplo
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                // procesar mensaje con la formula kelly.valoresRealesActuales[currentKelly*cantidad de datos + indice del campo]
                break;
            default:
                System.out.println("Trama "+msg[0]+" no procesada");
        } // switch
        if(found) {
            currentMsg++;
            if(currentMsg == 13) {
                currentMsg = 0;
                currentKelly = currentKelly == 1 ? 0 : 1;
            }
        }
        lastLen = L;
    } // parseMessage()
}
