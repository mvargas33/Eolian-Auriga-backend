package Test.Sandboxes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadSandbox {
    public static void main(String args[]) throws InterruptedException {
        long readDelay = 2000;
        SR sr1 = new SR("SR_01", readDelay);
        SR sr2 = new SR("SR_02", readDelay);
        SRVC service1 = new SRVC("Printer");
        sr1.subscribe(service1);
        sr2.subscribe(service1);
        List<SR> sr_list = new LinkedList<>();
        sr_list.add(sr1);
        sr_list.add(sr2);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(3);

        mainExecutor.submit(sr1);
        mainExecutor.submit(sr2);
        mainExecutor.submit(service1);

        mainExecutor.shutdown();

//        // SequentialRun
//        while(true){
//            for (SR sr: sr_list
//                 ) {
//                sr.sequentialRun();
//            }
//        }
        int READ_PERIOD = 100;

        int queueSizeAnterior = 0;
        int queueSizeActual = 0;
        int threshhold = 0;
        int confirmaciones = 0;
        while(true){
            Thread.sleep(200);
            queueSizeActual = service1.queueSize();
            if(queueSizeActual > queueSizeAnterior){
                if(confirmaciones == 5){ // 5 confirmaciones seguidas
                    threshhold = READ_PERIOD;
                    READ_PERIOD = READ_PERIOD * 2;
                    confirmaciones = 0;
                }else{
                    confirmaciones += 1;
                }
            }else{
                confirmaciones = 0; // basta 1 vez que baje la cola para reiniciar el contador
                if (READ_PERIOD - 10 > threshhold){
                    READ_PERIOD -= 10;
                }
            }
            System.out.println("New period: " + READ_PERIOD+ " Last queue size: " + queueSizeActual);
            for (SR sr:sr_list) {sr.setReadDelay(READ_PERIOD);}
            queueSizeAnterior = queueSizeActual;
        }
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

    public void setReadDelay(int readDelay){
        this.readDelay = readDelay;
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
        //long mem;
        while(true) {
            //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //if(System.currentTimeMillis() - lastRead> this.readDelay ){
                //System.out.println(this.name + " Memory used:" + mem);
                //System.out.println(this.name + " : Read made");
                for (SRVC service : myServices
                ) {
                    Object monitor = service.getMonitor();
                    synchronized (monitor) {
                        service.addToQueue(this); // Me agrego en cola
                        //System.out.println(this.name + " : Put in queue of " + service.getName());
                        monitor.notify();
                    }
                }
                //lastRead = System.currentTimeMillis();
                try {
                    Thread.sleep(this.readDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            //}
        }
    }

    public void sequentialRun() {
        //long mem;
        while(true){
            //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //System.out.println(this.name + " Memory used:" + mem);
            //System.out.println(this.name + " : Read made");
            for (SRVC service : myServices
            ) {
                service.sequentialRun(this);
            }
            try {
                Thread.sleep(this.readDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    public int queueSize(){
        return this.myReaders.size();
    }

    @Override
    public void run() {
        long mem;
        //System.out.println(this.name + " init.");
        while(true) {
            //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            while (!myReaders.isEmpty()) {
                SR sr = myReaders.poll();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //System.out.println(this.name + " : Sensor procesed: " + sr.getName());
            }
            //System.out.println(this.name + " Memory used:" + mem);
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sequentialRun(SR sr){
        long mem;
        mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        //System.out.println(this.name + " Memory used:" + mem);
        //System.out.println(this.name + " : Sensor procesed: " + sr.getName());
    }
}