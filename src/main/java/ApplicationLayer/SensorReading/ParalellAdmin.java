package ApplicationLayer.SensorReading;

import java.util.LinkedList;

public class ParalellAdmin implements Runnable{
    private LinkedList<SensorsReader> readers;

    public ParalellAdmin(){
        this.readers = new LinkedList<>();
    }

    public void addReader(SensorsReader reader){
        this.readers.add(reader);
    }

    public void addListOfReaders(LinkedList<SensorsReader> readers){
        this.readers.addAll(readers);
    }


    @Override
    public void run() {
        while(true){
            try{
                for (SensorsReader sr: readers
                ) {
                    sr.sequentialRun();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
