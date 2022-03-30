package ApplicationLayer.Channel;
import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class I2CTemp extends Channel{
    private I2CBus bus;
    private I2CDevice arduino0;
    public AppComponent arduino1;
    private int currentRegister;
    public AppComponent temp;
    private byte[] currentRegisterData;

    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices      Services to inform to whenever an AppComponents get updated
     */
    public I2CTemp(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);

        for(AppComponent ac : myComponentList) {
            if(ac.getID().toLowerCase().equals("temp")) {
                temp = ac;
            }
        }
    }

    @Override
    public void readingLoop() {
        while (true) {
            try {
                arduino0.write((byte) currentRegister);
                Thread.sleep(1000);
            //byte[] data = {(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
                //arduino0.read(data, 0, 8);
                //System.out.println(BitOperations.ArraytoString(data));
                byte[] data = {(byte) 0,(byte) 0};
                arduino0.read(data, 0, 2);
                double temp = data[0] + data[1]/100.0;
                this.temp.valoresRealesActuales[currentRegister] = temp;
                //parseMessageMPPT(data);
                currentRegister = (currentRegister + 1) % 2; // Ask for next BMS message
                this.informServices();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setUp() {
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            arduino0 = bus.getDevice(0x08);
            currentRegister = 0;
            currentRegisterData = new byte[7];
        } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
            e.printStackTrace();
        }
    }
}
