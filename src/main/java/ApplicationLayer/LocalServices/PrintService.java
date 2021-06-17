package ApplicationLayer.LocalServices;

import ApplicationLayer.AppComponents.AppComponent;

import java.util.Arrays;

/**
 * Simple print AppComponent's real values
 */
public class PrintService extends Service{
    private String printPrefix;

    public PrintService(){
        super();
    }

    public PrintService(String printPrefix){
        this.printPrefix = printPrefix;
    }
    @Override
    protected void serve(AppComponent c) {
        System.out.println(printPrefix + c.getID() + " : " + Arrays.toString(c.getValoresRealesActuales()));
    }
}
