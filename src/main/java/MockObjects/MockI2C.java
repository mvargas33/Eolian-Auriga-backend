package MockObjects;


/**
 * Simulates I2C data for the BMS and MPPT
 */
public class MockI2C {
  
    // It would be better to create controllers mockups and let them handle their canbus/i2c/other version of the message
    // For example, calling MockBMS.generateMsg101().toI2C() or MockBMS.generateMsg101().toCanbus().

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

    /**
     * 
     * @param offset
     * @return
     */
    
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

  

    /**
     * 
     * @param offset
     * @return
     */
    
    public byte[] msg101() {

        int packAbsCurrent  = ((data[0] & 0x00FF)<<8)|(data[1] & 0x00FF);
        int maximumPackVolt = ((data[2] & 0x00FF)<<8)|(data[3] & 0x00FF);
        int minimumPackVolt = ((data[4] & 0x00FF)<<8)|(data[5] & 0x00FF);
        valoresRealesActualesBMS[4] = packAbsCurrent;
        valoresRealesActualesBMS[5] = maximumPackVolt;
        valoresRealesActualesBMS[6] = minimumPackVolt;

        return data;
    }

    /**
     * 
     * @param offset
     * @return
     */
    
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

    /**
     * 
     * @param offset
     * @return
     */
    
    public byte[] msg081() {

        int thermistorID = data[0] & 0x00FF;
        int temperature = data[1] & 0x00FF;
        // revisar si thermistor id empieza en 1 o 0
        valoresRealesActualesBMS_TEMP[thermistorID-1] = temperature;

        return data;
    }

    /**
     * 
     * @param offset
     * @return
     */
    
    public byte[] msg082() {

        int thermistorID = data[1] & 0x00FF;
        int temperature = data[2] & 0x00FF;
        valoresRealesActualesBMS_TEMP[thermistorID-1] = temperature;

        return data;
    }

    /**
     * 
     * @param offset
     * @return
     */
    
    public byte[] msg036() {

        int cellID = (data[0] & 0x00FF);
        int voltage = ((data[1] & 0x00FF)<<8)|(data[2] & 0x00FF);
        valoresRealesActualesBMS_VOLT[cellID-1] = voltage;

        return data;
    }
    
}
