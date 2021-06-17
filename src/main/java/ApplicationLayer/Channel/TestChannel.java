package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TestChannel extends Channel{
    private HashMap<AppComponent, double[]> valuesMap;
    private final Random r;

    /**
     * Each channel has predefined AppComponents
     *  @param myComponentList List of AppComponent that this Channel update values to
     * @param myServices Services to inform to whenever an AppComponents get updated
     */
    public TestChannel(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);
        this.r = new Random();
        this.valuesMap = new HashMap<>();
        for (AppComponent a : myComponentList
             ) {
            valuesMap.put(a, new double[a.len]);
        }
    }

    /**
     * Updates a local double[] based on min/max values from AppComponent.
     * Then updates the double[] inside the AppComponent.
     * Then informs to services that this AppComponent has new values.
     */
    @Override
    public void readingLoop() {
        while(true){
            try{
                for (AppComponent a : myComponentList
                     ) {
                    // Update values directly. Without the call of updateValues() inside AppComponent
                    for (int i = 0; i < a.len; i++) {
                        a.valoresRealesActuales[i] = a.minimosConDecimal[i] + (a.maximosConDecimal[i] - a.minimosConDecimal[i]) * this.r.nextDouble(); // Random value in adequate range
                    }
                }
                super.informServices(); // Call this just after all AppComponent in myComponentList were updated
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    /**
     * No commands needed for Test random channel.
     */
    @Override
    public void setUp() {
    }
}
