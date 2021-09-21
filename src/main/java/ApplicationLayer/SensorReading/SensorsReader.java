package ApplicationLayer.SensorReading;

import ApplicationLayer.AppComponents.AppComponent;

public abstract class SensorsReader implements Runnable {
    public AppComponent myComponent;  // Componente al cual le encolará valores nuevos
    private long delayTime;
    public double[] values;                // Por optimización de memoria

    /**
     * Constructor base. Todos los SensorReaders están linkeados a un sólo AppComponent. No funcionan con receivers
     * @param myComponent AppComponent linkeado
     * @param readingDelayInMS Frecuencia de muestre
     */
    public SensorsReader(AppComponent myComponent, long readingDelayInMS) {
        this.myComponent = myComponent;
        this.delayTime = readingDelayInMS;
        this.values = new double[this.myComponent.len]; // Create an array same size as AppComponent values[]
    }

    /**
     * Métodos que deben implementar todos los tipos de lectores.
     * @return array de valores double[] con los nuevos valores del componente
     */
    public abstract void read(long delayTime);

    /**
     * Called by each SensorReader after they update the double[] values array.
     * @throws Exception
     */
    public void updateAndInformServices(){
        try {
            this.myComponent.updateValues(this.values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.myComponent.informToServices();
    }

    /**
     * Para control de delay
     * @param delayTimeMS
     */
    public synchronized void setReadDelay(long delayTimeMS){
        this.delayTime = delayTimeMS;
    }

    public synchronized AppComponent getMyComponent(){
        return this.myComponent;
    }

    /**
     * 0: Verifica que haya pasado tiempo suficiente para volver a leer.
     * 1: Lee nuevos valores.
     * 2: Los encola en el AppComponent correspondiente.
     * 3: Actualiza tiempos de lectura
     */
    @Override
    public void run() {
        this.read(delayTime); // Each SensorReader has a specific while loop and optimization
    }
    /**
     * Same as run(), without while() statement. Used by Sensor<Type>Admin
     */
    public void sequentialRun() {
        this.read(delayTime);                  // 1: Leer nuevos valores
//        myComponent.sequentialRun(values);          // Ejecuta secuancialmente todas las acciones hasta dejar los valores byte[] en la cola del Xbee
    }
}