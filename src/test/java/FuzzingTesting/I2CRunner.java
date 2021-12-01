package FuzzingTesting;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;



import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.ExcelToAppComponent.CSVToAppComponent;
import ApplicationLayer.Channel.I2C;
import MockObjects.BMSFenix;

public class I2CRunner {
    private I2CFuzzer fuzzer; // The fuzzer to generate random data
    private BMSFenix mock_bms; // The mockobject, class that holds the expected state of the mock_bms (used for assertEquals).
    private I2C channel; // The real channel, i.e the program to test.
    private String dir = "C:/Users/Dante/Desktop/Eolian/Eolian-Auriga-backend/src/main/java/ApplicationLayer/AppComponents/ExcelToAppComponent/Eolian_fenix";

    public I2CRunner(I2CFuzzer f) {
        fuzzer = f;
        mock_bms = new BMSFenix();
        List<AppComponent> lac = new ArrayList<>();
        try {
            lac = CSVToAppComponent.CSVs_to_AppComponents(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel = new I2C(lac, new ArrayList<>());
    } 

    public void run() {
        // The fuzzer passes through the mock object, to let it update the desired state of the component (bms in this example).
        channel.parseMessage100(mock_bms.msg100(fuzzer.fuzz())); // The mock object will process the data, and return it.
        // after fuzzing, updating the mock object and passing the data to the real channel, the new values should be correct
        assertArrayEquals(channel.bms.valoresRealesActuales, mock_bms.valoresRealesActualesBMS);
    }

    public void runs(int n) {
        for(int i = 0; i < n; i++) run();
    }


}
