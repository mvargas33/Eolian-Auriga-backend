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
import MockObjects.MockI2C;

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
        
        MockI2C mock_i2c = new MockI2C();
        byte offset = 0;
        I2C i2c = new I2C(lac, new ArrayList<>());

        for(byte i = 0; i < 10; i++) {
            offset = i;
            i2c.parseMessage100(mock_i2c.msg100());
            i2c.parseMessage101(mock_i2c.msg101());
            i2c.parseMessage102(mock_i2c.msg102());
            assertArrayEquals(mock_i2c.valoresRealesActualesBMS, i2c.bms.valoresRealesActuales);
            i2c.parseMessage081(mock_i2c.msg081());
            i2c.parseMessage082(mock_i2c.msg082());
            assertArrayEquals(mock_i2c.valoresRealesActualesBMS_TEMP, i2c.bms_temp.valoresRealesActuales);
            i2c.parseMessage036(mock_i2c.msg036());
            assertArrayEquals(mock_i2c.valoresRealesActualesBMS_VOLT, i2c.bms_volt.valoresRealesActuales);
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
