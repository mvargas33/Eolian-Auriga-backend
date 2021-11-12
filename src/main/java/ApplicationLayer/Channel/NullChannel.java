package ApplicationLayer.Channel;

import java.util.List;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

public class NullChannel extends Channel {

    public NullChannel(List<AppComponent> myComponentList, List<Service> myServices) {
        super(myComponentList, myServices);
    }
    
    @Override
    public void readingLoop() {
        // TODO Auto-generated method stub
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            super.informServices();
        }
        
    }

    @Override
    public void setUp() {
        // TODO Auto-generated method stub
        
    }
    
}
