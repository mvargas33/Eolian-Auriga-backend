package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

import java.util.List;

public class Canbus0 extends Channel {
    private final int valoresBMS = 133;
    private double[] bms_values;


    /**
     * Each channel has predefined AppComponents
     *
     * @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public Canbus0(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);
        // Check that a BMS AppComponent was supplied
        // With the exact amount of double[] values as the implementation here
        try{
            AppComponent bms = this.myComponentsMap.get("BMS"); // Must match name in .xlsx file
            if(bms != null){
                int len = bms.len;
                if(len != this.valoresBMS){
                    throw new Exception("Cantidad de valores de BMS en AppComponent != Cantidad de valores de lectura implementados");
                }
            }else{
                throw new Exception("A BMS AppComponent was not supplied in Canbus0 channel");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        // Init local array
        this.bms_values = new double[valoresBMS];
    }

    /**
     * Main reading and parsing loop
     */
    @Override
    public void readingLoop() {
        while(true){
            try{

            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    /**
     * Commands executed once
     */
    @Override
    public void init() {

    }
}
