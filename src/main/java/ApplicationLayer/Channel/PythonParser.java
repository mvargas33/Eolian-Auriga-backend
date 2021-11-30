package ApplicationLayer.Channel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class PythonParser {

    public static void main(String[] argv) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "python3 /home/pi/Desktop/lectura/COdigo_rendimiento_test.py");
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while(true){
                try{
                    while ((line = reader.readLine()) != null) {
                        new PythonParser().parseMessage(line);
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseMessage(String message) {
        String[] msg = message.split(","); // etter performance split than String.split()
        Scanner reader = new Scanner(System.in);
        // if (msg.length != 16){ // If it isn't CAN-type message
        //     System.out.println("Message is not CAN-type. Split length is not 16.");
        //     System.out.println(message);
        //     return;
        // }

        // Parse HEX strings to byte data type, into local buffer
        System.out.println(msg);
        switch (msg[0].split(":")[1]){
            case "100":
                System.out.println("V bat: "+ Double.parseDouble(msg[2].split(":")[1]) );//v bat
                System.out.println("C bat: "+ Double.parseDouble(msg[1].split(":")[1]) ); // current bat
                System.out.println("T inv: "+ Double.parseDouble(msg[3].split(":")[1]) ); //temp inv
                System.out.println("Pin: "  + Double.parseDouble(msg[4].split(":")[1]) ); // potin
                break;
            case "200":
                //'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+','+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)+'\n'
                System.out.println("Torque: "+ Double.parseDouble(msg[2].split(":")[1]) ); // torque
                System.out.println("rpm: "+ Double.parseDouble(msg[4].split(":")[1]) ); // rpm
                System.out.println("corriente motor: "+ Double.parseDouble(msg[1].split(":")[1]) );// corriente motor
                System.out.println("Pout: "  + Double.parseDouble(msg[5].split(":")[1]) ); // potout
                System.out.println("V: "  + Double.parseDouble(msg[3].split(":")[1]) ); // velocidad

                break;
            case "300":
                System.out.println("torque act: "+ Double.parseDouble(msg[3].split(":")[1]) ); // torque_act
                System.out.println("target lq: "+ Double.parseDouble(msg[1].split(":")[1]) ); // target lq
                System.out.println("target lq_hex: "+ Double.parseDouble(msg[4].split(":")[1]) ); // target lq_hex
                System.out.println("iq: "  + Double.parseDouble(msg[2].split(":")[1]) ); // lq
                System.out.println("iq_hex: "  + Double.parseDouble(msg[5].split(":")[1]) ); // lq_hex
                break;
            case "400":
                System.out.println("acelerador volt: "+Double.parseDouble(msg[1].split(":")[1]) );  // acelerador volt
                System.out.println("freno_volt: "+ Double.parseDouble(msg[3].split(":")[1]) ); // freno_volt
                break;
            default:
                System.out.println("Trama "+msg[0]+" no procesada");
        } // switch
        int n = reader.nextInt();
    } // parseMessage()
    
}
