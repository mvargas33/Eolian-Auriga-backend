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

public class I2C extends Channel{
    private I2CBus bus;
    private I2CDevice arduino0;
    private int currentMPPT;
    private byte[] currentMPPTData;

    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices      Services to inform to whenever an AppComponents get updated
     */
    public I2C(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);
    }

    @Override
    public void readingLoop() {
        while (true) {
            try {
                arduino0.write((byte) ((currentMPPT + 1) & 0xFF));
                Thread.sleep(1000);
                byte[] data = {(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
                arduino0.read(data, 0, 7);
                System.out.println(BitOperations.ArraytoString(data));
                parseMessage(data);
                Thread.sleep(1000);
                currentMPPT = (currentMPPT + 1) % 5; // Ask for next MPPT
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
            currentMPPT = 0;
            currentMPPTData = new byte[7];
        } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
            e.printStackTrace();
        }
    }

    void parseMessage(byte[] data){
        double BVLR = (data[0] & 0b10000000) >> 7;
        double OVT  = (data[0] & 0b01000000) >> 6;
        double NOC  = (data[0] & 0b00100000) >> 5;
        double UNDV = (data[0] & 0b00010000) >> 4;
        double Uin  = ((int)((int)((data[0] & 0b00000011) << 8) | (int) (data[1])))*0.15049;
        double Iin  = ((int)((int)((data[2] & 0b00000011) << 8) | (int)(data[3])))*0.00872;
        double Uout = ((int)((int)((data[4] & 0b00000011) << 8) | (int)(data[5])))*0.20879;
        double temp = data[6];
        System.out.print("ID________________________________________________");System.out.println("0x77" + (currentMPPT + 1));
        System.out.print("BVLR (1: Uout = Umax, 0: Uout < Umax)_____________");System.out.println(BVLR);
        System.out.print("OVT  (1: T>95°C, 0:T<95°C)________________________");System.out.println(OVT);
        System.out.print("NOC  (1: Batt. desconectada, 0: Batt. conectada)__");System.out.println(NOC);
        System.out.print("UNDV (1: Uin <= 26V, 0: Uin > 26V_________________");System.out.println(UNDV);
        System.out.print("Uin  (Voltage IN)_________________________________");System.out.print(Uin);System.out.println("\t[V]");
        System.out.print("Iin  (Current IN)_________________________________");System.out.print(Iin);System.out.println("\t[A]");
        System.out.print("Potencia generada_________________________________");System.out.print(Uin*Iin);System.out.println("\t[W]");
        System.out.print("Uout (Voltage OUT)________________________________");System.out.print(Uout);System.out.println("[V]");
        System.out.print("Temperature_______________________________________");System.out.print(temp);System.out.println("\t[°C]");
    }
}
