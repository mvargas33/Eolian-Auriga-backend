package Test.MultithreadTest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class SRVC implements Runnable{
    private String name;
    public Object monitor;
    public BlockingQueue<SR> myReaders;
    public int processedReaders;

    public SRVC(String name){
        this.name = name;
        this.monitor = new Object();
        this.myReaders = new LinkedBlockingQueue<>();
        this.processedReaders = 0;
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

    public int tick(){
        return this.processedReaders;
    }

    @Override
    public void run() {
        long mem;
        //System.out.println(this.name + " init.");
        SR sr;
        while(true) {
            //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            while (!myReaders.isEmpty()) {
                sr = myReaders.poll();
                try {
                    //System.out.println(this.getName() + ":" + sr.getName());
                    this.processedReaders++;
                    Thread.sleep(10); // Emula lo que demora enviar un mensaje por la Xbee
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

    public void sequentialRun(SR sr) throws InterruptedException {
        //System.out.println(this.getName() + ":" + sr.getName());
        this.processedReaders++;
        Thread.sleep(10); // Emula lo que demora enviar un mensaje por la Xbee
        //long mem;
        //mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        //System.out.println(this.name + " Memory used:" + mem);
        //System.out.println(this.name + " : Sensor procesed: " + sr.getName());
    }
}
