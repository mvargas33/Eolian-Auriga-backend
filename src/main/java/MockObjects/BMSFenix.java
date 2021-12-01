package MockObjects;

/**
 * Class that simulates the communication with the BMS, via CAN bus.
 * It holds it's complete state as well as the methods to send the data in CAN frames format.
 */
public class BMSFenix {

    public double[] valoresRealesActualesBMS = new double[15];
    public double[] valoresRealesActualesBMS_TEMP = new double[60];
    public double[] valoresRealesActualesBMS_VOLT = new double[30];
    public byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
  
    public void genData(int offset) {
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
        for(int i = 0; i < 8; i++) {
            data[i] += offset;
        }
        this.data = data;
    }

    public void genData(byte[] data) {
        this.data = data;
    }
    
    public byte[] msg100() {

        double packSOC         = (data[0] & 0x00FF)/2.0;
        double packCurrent     = ((data[1] & 0x00FF)<<8)|(data[2] & 0x00FF);
        int packInstVolt    = ((data[3]& 0x00FF)<<8)|(data[4]& 0x00FF);
        int packOpenVolt    = ((data[5]& 0x00FF)<<8)|(data[6]& 0x00FF);
        valoresRealesActualesBMS[0] = packSOC;
        valoresRealesActualesBMS[1] = packCurrent;
        valoresRealesActualesBMS[2] = packInstVolt;
        valoresRealesActualesBMS[3] = packOpenVolt;

        return data;
    }
    
    public byte[] msg101() {

        int packAbsCurrent  = ((data[0] & 0x00FF)<<8)|(data[1] & 0x00FF);
        int maximumPackVolt = ((data[2] & 0x00FF)<<8)|(data[3] & 0x00FF);
        int minimumPackVolt = ((data[4] & 0x00FF)<<8)|(data[5] & 0x00FF);
        valoresRealesActualesBMS[4] = packAbsCurrent;
        valoresRealesActualesBMS[5] = maximumPackVolt;
        valoresRealesActualesBMS[6] = minimumPackVolt;

        return data;
    }

    public byte[] msg102() {

        int highTemperature   = data[0] & 0x00FF;
        int highThermistorID  = data[1] & 0x00FF;
        int lowTemperature    = data[2] & 0x00FF;
        int lowThermistorID   = data[3] & 0x00FF;
        int avgTemp           = data[4] & 0x00FF;
        int internalTemp      = data[5] & 0x00FF;
        int max_volt_id       = data[6] & 0x00FF;
        int min_volt_id       = data[7] & 0x00FF;
        valoresRealesActualesBMS[7] = highTemperature;
        valoresRealesActualesBMS[8] = highThermistorID;
        valoresRealesActualesBMS[9] = lowTemperature;
        valoresRealesActualesBMS[10] = lowThermistorID;
        valoresRealesActualesBMS[11] = avgTemp;
        valoresRealesActualesBMS[12] = internalTemp;
        valoresRealesActualesBMS[13] = max_volt_id;
        valoresRealesActualesBMS[14] = min_volt_id;

        return data;
    }

    public byte[] msg081() {

        int thermistorID = data[0] & 0x00FF;
        int temperature = data[1] & 0x00FF;
        // revisar si thermistor id empieza en 1 o 0
        valoresRealesActualesBMS_TEMP[thermistorID-1] = temperature;

        return data;
    }

    public byte[] msg082() {

        int thermistorID = data[1] & 0x00FF;
        int temperature = data[2] & 0x00FF;
        valoresRealesActualesBMS_TEMP[thermistorID-1] = temperature;

        return data;
    }

    public byte[] msg036() {

        int cellID = (data[0] & 0x00FF);
        int voltage = ((data[1] & 0x00FF)<<8)|(data[2] & 0x00FF);
        valoresRealesActualesBMS_VOLT[cellID-1] = voltage;
        return data;
    }

    public byte[] msg100(byte[] new_data) {
        genData(new_data);
        return msg100();
    }

  
    public byte[] msg101(byte[] new_data) {
        genData(new_data);
        return msg101();
    }
    
    public byte[] msg102(byte[] new_data) {
        genData(new_data);
        return msg102();
    }
    
    public byte[] msg081(byte[] new_data) {
        genData(new_data);
        return msg081();
    }
    
    public byte[] msg082(byte[] new_data) {
        genData(new_data);
        return msg082();
    }

    public byte[] msg036(byte[] new_data) {
        genData(new_data);
        return msg036();
    }
    
}
