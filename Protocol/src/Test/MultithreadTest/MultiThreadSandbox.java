package Test.MultithreadTest;

import com.sun.org.apache.xpath.internal.operations.Mult;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadSandbox {

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @State(Scope.Thread)
    public static class MyState {
        long readDelay = 160;
        int SR_size = 10;
        public SRVC service1 = new SRVC("Printer");
        public List<SR> sr_list = new LinkedList<>();
        public long TIMEOUT = 10000;

        public MyState(){
            for (int i = 0; i< SR_size; i++){
                sr_list.add(new SR("SR_" + Integer.toString(i),readDelay));
            }
            for (SR sr : sr_list
                 ) {
                sr.subscribe(service1);
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void linearExecution(MyState state) throws InterruptedException {
        long last = System.currentTimeMillis();
        long now;

//        while (now - init < state.TIMEOUT) {
        double diff;
        double counter = 0;
        while(true){
            for (SR sr : state.sr_list
            ) {
                sr.sequentialRun();
            }
            if( counter == 100){
                now = System.currentTimeMillis();
                diff = ((double) now - (double)last) / counter;

                System.out.println("Avg delay per SR in last " + counter + " times: " + diff);
                counter = 0;
                last = now;
            }
            counter++;
        }

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void paralellExecution(MyState state) {
        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(state.SR_size + 1);
        for (int i = 0; i < state.SR_size; i++) {
            mainExecutor.submit(state.sr_list.get(i));
        }
        mainExecutor.submit(state.service1);
        //mainExecutor.submit(new Ticker(state.service1));
//        Ticker t = new Ticker(state.service1);
//        t.run();

        //state.service1.run();
        mainExecutor.shutdown();
    }

    static class LinearExecution implements Runnable {
        MultiThreadSandbox ms;
        MyState mss;

        public LinearExecution(MultiThreadSandbox ms, MyState mss) {
            this.ms = ms;
            this.mss = mss;
        }
        @Override
        public void run() {
            try {
                ms.linearExecution(mss);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void linearTest() throws InterruptedException {
        MultiThreadSandbox ms = new MultiThreadSandbox();
        MyState myState = new MyState();

        // Linear with test
        ExecutorService mainExecutor = Executors.newFixedThreadPool(1);
        mainExecutor.submit(new LinearExecution(ms, myState));
        mainExecutor.shutdown();

        ms.serviceTicker(myState.service1);
    }

    public void paralellTest() throws Exception{
        MultiThreadSandbox ms = new MultiThreadSandbox();
        MyState myState = new MyState();

        // Paralell
        ms.paralellExecution(myState);

        // Delay Control
        ExecutorService mainExecutor = Executors.newFixedThreadPool(1);
        mainExecutor.submit(new DelayControl(myState.service1, myState.sr_list));
        mainExecutor.shutdown();

        // Ticker sampler
        ms.serviceTicker(myState.service1);
    }

    public static void main(String args[]) throws Exception {
        MultiThreadSandbox ms = new MultiThreadSandbox();
        ms.paralellTest();
        //ms.linearTest();
    }

    // Adjust delay of SR's readings to stop the growing of services Queues
    public void delayControl(SRVC service1, List<SR> sr_list) throws InterruptedException {
        int READ_PERIOD = 160; // 1x delay

        int queueSizeAnterior = 0;
        int queueSizeActual = 0;
        int threshhold = 0;
        int confirmaciones = 0;
        int estancado = 0;

        while (true) {
            Thread.sleep(160*3); // 3x Delay
            queueSizeActual = service1.queueSize();

            if (queueSizeActual > queueSizeAnterior) {
                confirmaciones += 1;
                if (confirmaciones == 5) { // 5 confirmaciones seguidas
                    threshhold = READ_PERIOD; // "Este read_period no me sirve" "debe ser mayor"
                    READ_PERIOD = (int) ((double) READ_PERIOD * 1.5); // Sube exponencialmente para poder recorrer la cola
                    confirmaciones = 0;
                    service1.resetQueue();
                }
            } else {
                confirmaciones = 0; // basta 1 vez que baje la cola para reiniciar el contador
                if (READ_PERIOD - 1 > threshhold) { // "Si bajo, estoy sobre el límite inferior?"
                    READ_PERIOD -= 1; // Baja linealmente
                }
            }

            // Si se queda estancado, vaciar la Queue
            if (queueSizeActual > 0 && (queueSizeActual == queueSizeAnterior | queueSizeActual + 1 == queueSizeAnterior | queueSizeActual - 1 == queueSizeAnterior)){
                estancado = estancado + 1;
                //System.out.println("ESTANCADO: " + estancado);
                if(estancado == 10){
                    //System.out.println("ESTANCADO 10");
                    service1.resetQueue();
                    estancado = 0;
                }
            }

            System.out.println("New period: " + READ_PERIOD + " Last queue size: " + queueSizeActual);
            for (SR sr : sr_list) {
                sr.setReadDelay(READ_PERIOD);
            }
            queueSizeAnterior = queueSizeActual;
        }
    }

    public void serviceTicker(SRVC service) throws InterruptedException {
        long sampleTime = 1000;
        double times = 0;
        double t;
        double last =0;
        while (true) {
            times++;
            t = service.tick();
            System.out.println(service.getName() + " Time [s]: " + Double.toString(times) + " SR processed: " + Double.toString(t) + " Diff: " + Double.toString(t-last) + " SR/s : " + Double.toString(t / times));
            last = t;
            Thread.sleep(sampleTime);
        }
    }

    class Ticker implements Runnable {
        private SRVC myService;

        Ticker(SRVC service){
            this.myService = service;
        }

        @Override
        public void run() {
            MultiThreadSandbox ms = new MultiThreadSandbox();
            try {
                ms.serviceTicker(this.myService);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class DelayControl implements Runnable {
        private SRVC myService;
        private List<SR> sr_list;

        DelayControl(SRVC service, List<SR> sr_list){
            this.myService = service;
            this.sr_list = sr_list;
        }

        @Override
        public void run() {
            MultiThreadSandbox ms = new MultiThreadSandbox();
            try {
                ms.delayControl(this.myService, this.sr_list);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

