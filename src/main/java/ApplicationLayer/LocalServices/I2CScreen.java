package ApplicationLayer.LocalServices;

import java.io.IOException;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

public class I2CScreen extends Service {
    
    private I2CBus bus;
    private I2CDevice arduinoLCD;
    public AppComponent lcd;

    public I2CScreen(List<AppComponent> myComponentList) {
        super();

        for(AppComponent ac : myComponentList) {
            if(ac.getID().toLowerCase().equals("lcd")) {
                lcd = ac;
            }
        }

        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);;
            arduinoLCD = bus.getDevice(0x08);
        } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serve(AppComponent c) {
        // TODO Auto-generated method stub
        try {
            arduinoLCD.write((byte) 1);
            int b1 = (int) lcd.valoresRealesActuales[0]; //parte entera del P
            int b2 = (int) lcd.valoresRealesActuales[0]*100%100; //primeros 2 decimales del P
            arduinoLCD.write((byte) b1);
            arduinoLCD.write((byte) b2);

            arduinoLCD.write((byte) 2);
            b1 = (int) lcd.valoresRealesActuales[2]; //parte entera del P
            b2 = (int) lcd.valoresRealesActuales[2]*100%100; //primeros 2 decimales del P
            arduinoLCD.write((byte) b1);
            arduinoLCD.write((byte) b2);

            arduinoLCD.write((byte) 3);
            b1 = (int) lcd.valoresRealesActuales[3]; //parte entera del P
            b2 = (int) lcd.valoresRealesActuales[3]*100%100; //primeros 2 decimales del P
            arduinoLCD.write((byte) b1);
            arduinoLCD.write((byte) b2);

            arduinoLCD.write((byte) 4);
            b1 = (int) lcd.valoresRealesActuales[1]; //parte entera del P
            b2 = (int) lcd.valoresRealesActuales[1]*100%100; //primeros 2 decimales del P
            arduinoLCD.write((byte) b1);
            arduinoLCD.write((byte) b2);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//1 send P
    }
    
}
