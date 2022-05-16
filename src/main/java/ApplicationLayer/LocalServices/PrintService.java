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
    public void serve(AppComponent c) {
        //System.out.println(Arrays.toString(c.nombreParametros));
        //System.out.println(printPrefix + c.getID() + " : " + Arrays.toString(c.getValoresRealesActuales()));
        for(int i = 0; i < c.valoresRealesActuales.length; i++) {
            System.out.print(c.nombreParametros[i]+":"+c.valoresRealesActuales[i]+", ");
        }
        System.out.println("\n");
    }
}
