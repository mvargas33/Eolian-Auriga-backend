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
    public AppComponent arduino1;
    private int currentRegister;
    public AppComponent bms;
    public AppComponent bms_temp;
    public AppComponent bms_volt;
    public AppComponent mppt1;
    public AppComponent mppt2;
    public AppComponent mppt3;
    public AppComponent mppt4;
    public AppComponent kelly_der;
    public AppComponent kelly_izq;
    public AppComponent main_data;
    private byte[] currentRegisterData;

    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices      Services to inform to whenever an AppComponents get updated
     */
    public I2C(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);

        for(AppComponent ac : myComponentList) {
            if(ac.getID().toLowerCase().equals("bms")) {
                bms = ac;
            }
            else if(ac.getID().toLowerCase().equals("bms_volt")) {
                bms_volt = ac;
            }
            else if(ac.getID().toLowerCase().equals("bms_temp")) {
                bms_temp = ac;
            }
            else if(ac.getID().toLowerCase().equals("mainData")) {
                main_data = ac;
            }
            else if(ac.getID().toLowerCase().equals("kelly_der")) {
                kelly_der = ac;
            }
            else if(ac.getID().toLowerCase().equals("kelly_izq")) {
                kelly_izq = ac;
            }
            else if(ac.getID().toLowerCase().equals("mppt1")) {
                mppt1 = ac;
            }
            else if(ac.getID().toLowerCase().equals("mppt2")) {
                mppt2 = ac;
            }
            else if(ac.getID().toLowerCase().equals("mppt3")) {
                mppt3 = ac;
            }
            else if(ac.getID().toLowerCase().equals("mppt4")) {
                mppt4 = ac;
            }
        }
    }

    @Override
    public void readingLoop() {
        while (true) {
            try {
                arduino0.write((byte) ((currentRegister + 1) & 0xFF));
                Thread.sleep(1000);
                byte[] data = {(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
                arduino0.read(data, 0, 8);
                System.out.println(BitOperations.ArraytoString(data));
                switch (currentRegister + 1){
                    case 1:
                        parseMessage100(data);
                        break;
                    case 2:
                        parseMessage101(data);
                        break;
                    case 3:
                        parseMessage102(data);
                        break;
                    case 4:
                        parseMessage081(data);
                        break;
                    case 5:
                        parseMessage082(data);
                        break;
                    case 6:
                        parseMessage036(data);
                        break;
                    default:
                        System.out.println(BitOperations.ArraytoString(data));
                }

                //parseMessageMPPT(data);
                Thread.sleep(1000);
                currentRegister = (currentRegister + 1) % 6; // Ask for next BMS message
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

    void parseMessageMPPT(byte[] data){
        double BVLR = (data[0] & 0b10000000) >> 7;
        double OVT  = (data[0] & 0b01000000) >> 6;
        double NOC  = (data[0] & 0b00100000) >> 5;
        double UNDV = (data[0] & 0b00010000) >> 4;
        double Uin  = ((int)((int)((data[0] & 0b00000011) << 8) | (int) (data[1] & 0x00FF)))*0.15049;
        double Iin  = ((int)((int)((data[2] & 0b00000011) << 8) | (int) (data[3] & 0x00FF)))*0.00872;
        double Uout = ((int)((int)((data[4] & 0b00000011) << 8) | (int) (data[5] & 0x00FF)))*0.20879;
        double temp = data[6];
        System.out.print("ID________________________________________________");System.out.println("0x77" + (currentRegister + 1));
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

    void parseMessage100(byte[] data){
        double packSOC         = (data[0] & 0x00FF)/2.0;
        double packCurrent     = ((data[1] & 0x00FF)<<8)|(data[2] & 0x00FF);
        int packInstVolt    = ((data[3]& 0x00FF)<<8)|(data[4]& 0x00FF);
        int packOpenVolt    = ((data[5]& 0x00FF)<<8)|(data[6]& 0x00FF);
        System.out.print("PACK_SOC:_______");System.out.println(packSOC);
        System.out.print("PACK_CURRENT:___");System.out.println(packCurrent);
        System.out.print("PACK_INST_VTG:__");System.out.println(packInstVolt);
        System.out.print("PACK_OPEN_VTG:__");System.out.println(packOpenVolt);
        bms.valoresRealesActuales[0] = packSOC;
        bms.valoresRealesActuales[1] = packCurrent;
        bms.valoresRealesActuales[2] = packInstVolt;
        bms.valoresRealesActuales[3] = packOpenVolt;
    }
    void parseMessage101(byte[] data){
        int packAbsCurrent  = ((data[0] & 0x00FF)<<8)|(data[1] & 0x00FF);
        int maximumPackVolt = ((data[2] & 0x00FF)<<8)|(data[3] & 0x00FF);
        int minimumPackVolt = ((data[4] & 0x00FF)<<8)|(data[5] & 0x00FF);
        System.out.print("PACK_ABSCURRENT:");System.out.println(packAbsCurrent);
        System.out.print("MAXIM_PACK_VTG:_");System.out.println(maximumPackVolt);
        System.out.print("MINIM_PACK_VTG:_");System.out.println(minimumPackVolt);
        bms.valoresRealesActuales[4] = packAbsCurrent;
        bms.valoresRealesActuales[5] = maximumPackVolt;
        bms.valoresRealesActuales[6] = minimumPackVolt;
    }
    void parseMessage102(byte[] data){
        int highTemperature   = data[0] & 0x00FF;
        int highThermistorID  = data[1] & 0x00FF;
        int lowTemperature    = data[2] & 0x00FF;
        int lowThermistorID   = data[3] & 0x00FF;
        int avgTemp           = data[4] & 0x00FF;
        int internalTemp      = data[5] & 0x00FF;
        int max_volt_id       = data[6] & 0x00FF;
        int min_volt_id       = data[7] & 0x00FF;
        System.out.print("HIGH_TEMP,");System.out.println(highTemperature);
        System.out.print("LOW_TEMP,");System.out.println(lowTemperature);
        System.out.print("HIGH_TID,");System.out.println(highThermistorID);
        System.out.print("LOW_TID,");System.out.println(lowThermistorID);
        System.out.print("AVG_TEMP,");System.out.println(avgTemp);
        System.out.print("INT_TEMP,");System.out.println(internalTemp);
        System.out.print("MAX_VOLT_ID,");System.out.println(max_volt_id);
        System.out.print("MIN_VOLT_ID,");System.out.println(min_volt_id);
        bms.valoresRealesActuales[7] = highTemperature;
        bms.valoresRealesActuales[8] = highThermistorID;
        bms.valoresRealesActuales[9] = lowTemperature;
        bms.valoresRealesActuales[10] = lowThermistorID;
        bms.valoresRealesActuales[11] = avgTemp;
        bms.valoresRealesActuales[12] = internalTemp;
        bms.valoresRealesActuales[13] = max_volt_id;
        bms.valoresRealesActuales[14] = min_volt_id;
    }
    void parseMessage081(byte[] data){
        int thermistorID = data[0] & 0x00FF;
        int temperature = data[1] & 0x00FF;
        System.out.print("Temperatura Nro ");System.out.print(thermistorID);System.out.print(": ");System.out.println(temperature);
        // revisar si thermistor id empieza en 1 o 0
        bms_temp.valoresRealesActuales[thermistorID-1] = temperature;
    }
    void parseMessage082(byte[] data){
        int thermistorID = data[1] & 0x00FF;
        int temperature = data[2] & 0x00FF;
        System.out.print("Temperatura Nro ");System.out.print(thermistorID);System.out.print(": ");System.out.println(temperature);
        bms_temp.valoresRealesActuales[thermistorID-1] = temperature;
    }
    void parseMessage036(byte[] data){
        int cellID = (data[0] & 0x00FF);
        int voltage = ((data[1] & 0x00FF)<<8)|(data[2] & 0x00FF);
        System.out.print("Voltaje Nro ");System.out.print(cellID);System.out.print(": ");System.out.println(voltage);
        bms_volt.valoresRealesActuales[cellID-1] = voltage;
    }
}
