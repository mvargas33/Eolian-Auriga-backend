/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ApplicationLayer.LocalServices;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.Utils.I2CLCD;

/**
 * Corriente potencia marcha
 * @author user
 */
public class LCDScreen1 extends Service {

    I2CDevice device = null;
    I2CLCD lcd = null;

    public LCDScreen1(int id) {
        try {
            I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
            device = bus.getDevice(id);
            lcd = new I2CLCD(device);
            lcd.init();
            lcd.backlight(true);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        lcd.display_string_pos("Po:", 1, 0);
        lcd.display_string_pos("C:", 2, 0);
    } 

    @Override
    public void serve(AppComponent c) {
        if(c.getID().equals("lcd")) {
            String l1 = String.format("%.2fW          ", c.valoresRealesActuales[0]); //PO
            String l2 = String.format("%.3fA          ", c.valoresRealesActuales[2]); //C
            lcd.display_string_pos(l1, 1, 4);
            lcd.display_string_pos(l2, 2, 3);
        }
        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }  
    }

}