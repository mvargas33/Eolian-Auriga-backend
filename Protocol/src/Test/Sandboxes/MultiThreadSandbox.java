package Test.Sandboxes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadSandbox {
    public static void main(String args[]) {
        long readDelay = 2000;
        SR sr1 = new SR("SR_01", readDelay);
        SR sr2 = new SR("SR_02", readDelay);
        SRVC service1 = new SRVC("Printer");
        sr1.subscribe(service1);
        sr2.subscribe(service1);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(3);

        mainExecutor.submit(sr1);
        mainExecutor.submit(sr2);
        mainExecutor.submit(service1);

        mainExecutor.shutdown();
    }

}

// SensorReader dummy
class SR implements Runnable{
    private String name;
    private List<SRVC> myServices;
    private long readDelay;

    public SR(String name, long readDelayMillis){
        this.name = name;
        this.myServices = new ArrayList<>();
        this.readDelay = readDelayMillis;
    }

    public void subscribe(SRVC service){
        this.myServices.add(service);
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        System.out.println(this.name + " init.");
        long lastRead = System.currentTimeMillis();
        while(true) {
            if(System.currentTimeMillis() - lastRead> this.readDelay ){
                System.out.println(this.name + " : Read made");
                for (SRVC service : myServices
                ) {
                    Object monitor = service.getMonitor();
                    synchronized (monitor) {
                        service.addToQueue(this); // Me agrego en cola
                        System.out.println(this.name + " : Put in queue of " + service.getName());
                        monitor.notify();
                    }
                }
                lastRead = System.currentTimeMillis();
            }
        }
    }
}

// Service dummy
class SRVC implements Runnable{
    private String name;
    public Object monitor;
    public BlockingQueue<SR> myReaders;

    public SRVC(String name){
        this.name = name;
        this.monitor = new Object();
        this.myReaders = new LinkedBlockingQueue<>();
    }

    public Object getMonitor(){
        return this.monitor;
    }

    public String getName() {
        return name;
    }

    public void addToQueue(SR sensor){
        this.myReaders.add(sensor);
    }

    @Override
    public void run() {
        System.out.println(this.name + " init.");
        while(true) {
            while (!myReaders.isEmpty()) {
                SR sr = myReaders.poll();
                System.out.println(this.name + " : Sensor procesed: " + sr.getName());
            }
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}