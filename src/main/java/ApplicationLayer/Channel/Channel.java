package ApplicationLayer.Channel;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.LocalServices.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Channel implements Runnable {
    protected HashMap<String, AppComponent> myComponentsMap; // To modify values with cost O(1) inside readingLoop()
    private final List<AppComponent> myComponentList ; // List that Services will receive
    private final List<Service> myServices; // List of services that need to know about myComponents updates

    /**
     * Each channel has predefined AppComponents
     * @param myComponentList List of AppComponent that this Channel update values to
     */
    public Channel(List<AppComponent> myComponentList, List<Service> myServices) {
        this.myServices = myServices;
        this.myComponentList = myComponentList;
        this.myComponentsMap = new HashMap<>();
        for (AppComponent a: myComponentList
             ) {
            this.myComponentsMap.put(a.ID, a);
        }
    }

    /**
     * Commands executed recurrently. Parsing process.
     * Has a while True loop.
     * At the end of each reading, it executes informServices()
     */
    public abstract void readingLoop();

    /**
     * Any command that only needs to be executed once
     */
    public abstract void init();

    /**
     * Method executed once the channel updates all his AppComponents.
     * Called at the end of one reading cycle.
     */
    public void informServices(){
        for (Service s : this.myServices
             ) {
            s.putListOfComponentsInQueue(myComponentList);
        }
    }

    /**
     * Init the channel, and the loop.
     */
    @Override
    public void run() {
        try{
            this.init();
            this.readingLoop();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
