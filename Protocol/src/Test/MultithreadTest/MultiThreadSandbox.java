package Test.MultithreadTest;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadSandbox {

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @State(Scope.Thread)
    public static class MyState{
        long readDelay = 2000;
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

        while(now - init < state.TIMEOUT){
        for (SR sr: state.sr_list
             ) {
            sr.sequentialRun();
        }
            now = System.currentTimeMillis();
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
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);

        mainExecutor.submit(state.sr1);
        mainExecutor.submit(state.sr2);
        long init = System.currentTimeMillis();
        long now = System.currentTimeMillis();

        mainExecutor.submit(state.service1);

        //mainExecutor.shutdown();
    }


    public static void main(String args[]) throws InterruptedException {
        MultiThreadSandbox ms = new MultiThreadSandbox();
        MyState myState = new MyState();

        // Linear
        ms.linearExecution(myState);

        // Paralell
        //ms.paralellExecution(myState);
        //s.delayControl(myState.service1, myState.sr_list); // Delay control

    }

    // Adjust delay of SR's readings to stop the growing of services Queues
    public void delayControl(SRVC service1, List<SR> sr_list) throws InterruptedException {
        int READ_PERIOD = 100;

        int queueSizeAnterior = 0;
        int queueSizeActual = 0;
        int threshhold = 0;
        int confirmaciones = 0;
        while(true){
            Thread.sleep(100);
            queueSizeActual = service1.queueSize();
            if(queueSizeActual > queueSizeAnterior){
                if(confirmaciones == 5){ // 5 confirmaciones seguidas
                    threshhold = READ_PERIOD;
                    READ_PERIOD = READ_PERIOD * 10;
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
