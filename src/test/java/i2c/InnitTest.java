package i2c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import org.junit.jupiter.api.Test;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.I2C;
import MockObjects.BMSFenix;

public class InnitTest {
    
    // ID of the arduino in the i2c protocol
    public int ID = 0x08;
    public int BUS_N = I2CBus.BUS_1;

    @Test
    public void checkConnection() throws I2CFactory.UnsupportedBusNumberException, IOException{
        // It would be better to check directly from the class (calling something like channel.setUp)
        // rather than copying its implementation
        try {
            I2CBus i2c_bus = I2CFactory.getInstance(I2CBus.BUS_1);
            I2CDevice device = i2c_bus.getDevice(ID);
        }
        catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
            String e_msg = "Couldn't establish a connection with the i2c device, check the BUS number and the slave ID. BUS NUMBER: " + BUS_N + ", SLAVE ID: 0x" +String.format("%02X", ID);;
            throw new IOException(e_msg, e);
        }
    }

    @Test
    public void checkFunctionality() throws Exception {
        // preocuparse de que funciona para 3 iteraciones minimo
        List<AppComponent> lac = CSVToAppComponent.CSVs_to_AppComponents("components/Eolian_fenix");
        
        BMSFenix bms = new BMSFenix();
        byte offset = 0;
        I2C i2c = new I2C(lac, new ArrayList<>());

        for(byte i = 0; i < 10; i++) {
            offset = i;
            i2c.parseMessage100(bms.msg100());
            i2c.parseMessage101(bms.msg101());
            i2c.parseMessage102(bms.msg102());
            assertArrayEquals(bms.valoresRealesActualesBMS, i2c.bms.valoresRealesActuales);
            i2c.parseMessage081(bms.msg081());
            i2c.parseMessage082(bms.msg082());
            assertArrayEquals(bms.valoresRealesActualesBMS_TEMP, i2c.bms_temp.valoresRealesActuales);
            i2c.parseMessage036(bms.msg036());
            assertArrayEquals(bms.valoresRealesActualesBMS_VOLT, i2c.bms_volt.valoresRealesActuales);
            bms.genData(offset);
        }
        //i2c.parseMessageMPPT(data);
    }

    @Test
    public void realReader() throws Exception {
        // This method should test that the values of the controllers are updated, using just the channel and the real controller conection
        // Example pseudo code
        /*
        lac = list_of_app_components;
        I2C i2c = new I2C(lac, []);
        i2c.run();
        for each reading loop of the i2c.channel {
            assertArrayEquals(expectedValuesArray, i2c.valoresRealesActuales);
        }
        */
    }

}
