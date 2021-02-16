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
        long readDelay = 0;
        public SR sr1 = new SR("SR_01", readDelay);
        public SR sr2 = new SR("SR_02", readDelay);
        public SRVC service1 = new SRVC("Printer");
        public List<SR> sr_list = new LinkedList<>();

        public long TIMEOUT = 10000;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void linearExecution(MyState state) throws InterruptedException {
        state.sr1.subscribe(state.service1);
        state.sr2.subscribe(state.service1);
        state.sr_list.add(state.sr1);
        state.sr_list.add(state.sr2);

        long init = System.currentTimeMillis();
        long now = System.currentTimeMillis();

//        while (now - init < state.TIMEOUT) {
        while(true){
            for (SR sr : state.sr_list
            ) {
                sr.sequentialRun();
            }
//            now = System.currentTimeMillis();
        }

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void paralellExecution(MyState state) {
        state.sr1.subscribe(state.service1);
        state.sr2.subscribe(state.service1);
        state.sr_list.add(state.sr1);
        state.sr_list.add(state.sr2);

        // Execute threads
        ExecutorService mainExecutor = Executors.newFixedThreadPool(3);

        mainExecutor.submit(state.sr1);
        mainExecutor.submit(state.sr2);
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
        int READ_PERIOD = 100;

        int queueSizeAnterior = 0;
        int queueSizeActual = 0;
        int threshhold = 0;
        int confirmaciones = 0;
        while (true) {
            Thread.sleep(100);
            queueSizeActual = service1.queueSize();
            if (queueSizeActual > queueSizeAnterior) {
                if (confirmaciones == 5) { // 5 confirmaciones seguidas
                    threshhold = READ_PERIOD;
                    READ_PERIOD = READ_PERIOD * 10;
                    confirmaciones = 0;
                } else {
                    confirmaciones += 1;
                }
            } else {
                confirmaciones = 0; // basta 1 vez que baje la cola para reiniciar el contador
                if (READ_PERIOD - 10 > threshhold) {
                    READ_PERIOD -= 10;
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
            t = (double) service.tick();
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

