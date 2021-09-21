package ApplicationLayer.SensorReading.RandomReaders;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.SensorReading.SensorsReader;

import java.util.Random;

/**
 * Clase que se encarga de generar datos al azar para simular componentes
 */
public class RandomReader extends SensorsReader{
    private final Random r;

    /**
     * Constructor base. Todos los SensorReaders están linkeados a un sólo AppComponent. No funcionan con receivers
     *
     * @param myComponent      AppComponent linkeado
     * @param readingDelayInMS Frecuencia de muestre
     */
    public RandomReader(AppComponent myComponent, long readingDelayInMS) {
        super(myComponent, readingDelayInMS);
        this.r = new Random();
    }

    public void actualRead(){
        for (int i = 0; i < this.myComponent.len; i++) {
            super.values[i] = myComponent.minimosConDecimal[i] + (myComponent.maximosConDecimal[i] - myComponent.minimosConDecimal[i]) * this.r.nextDouble(); // Generar valor random en el rango adecuado
        }
        super.updateAndInformServices();
    }

    @Override
    public void read(long delayTime) {
        while(true) {
            actualRead();
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}
