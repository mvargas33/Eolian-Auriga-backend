package ApplicationLayer;

import java.util.ArrayList;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

public class Manager implements Runnable {
    
    public ArrayList<Service> services;
    public AppComponent ac;

    public Manager(ArrayList<Service> services, AppComponent ac) {
        this.services = services;
        this.ac = ac;
    }

    @Override
    public void run() {
        for(Service s : services) {
            s.serve(ac);
        }
    }
}
