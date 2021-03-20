package Test.MultithreadTest;

import java.util.ArrayList;
import java.util.List;

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
        Object monitor;
        while(true) {
            //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //if(System.currentTimeMillis() - lastRead> this.readDelay ){
            //System.out.println(this.name + " Memory used:" + mem);
            //System.out.println(this.name + " : Read made");
            for (SRVC service : myServices
            ) {
                monitor = service.getMonitor();
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

    public void sequentialRun() throws InterruptedException {
        //long mem;
        //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        //System.out.println(this.name + " Memory used:" + mem);
        //System.out.println(this.name + " : Read made");
        for (SRVC service : myServices
        ) {
            service.sequentialRun(this);
        }

    }
}