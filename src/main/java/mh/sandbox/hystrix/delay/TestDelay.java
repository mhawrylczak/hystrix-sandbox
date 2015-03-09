package mh.sandbox.hystrix.delay;


import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import rx.functions.Action1;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class TestDelay {

    private final static Logger logger = LoggerFactory.getLogger(TestDelay.class);

    public static int poolSize = 10;
    public final static long tasks = 1;
    public final static AtomicLong tasksSubmitted = new AtomicLong(tasks);

    public  static LongAdder count = new LongAdder();
    public  static LongAdder startTimes = new LongAdder();
    public  static LongAdder endTimes = new LongAdder();
    public static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static Semaphore semaphore = new Semaphore(poolSize);


//    static{
//        HystrixPlugins.getInstance().registerConcurrencyStrategy(MdcHystrixConcurrencyStrategy.getInstance());
//    }

    public static void main(String[] args) throws InterruptedException {
//        HystrixPlugins.getInstance().registerConcurrencyStrategy(new TestHystrixConcurrencyStrategy());
        MDC.put("orher", "ss");

//        new TestCommand().observe();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("start");


        for( int i =0 ; i < 4; i++) {
            new Thread(() -> {
                for (; ; ) {
                    long taskLeft = tasksSubmitted.decrementAndGet();
                    if (taskLeft < 0) {
                        return;
                    }
                    executorService.submit(() -> {
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        MDC.put("smth", "32323");
                        new TestCommand().observe().subscribe(stats -> {
//                        logger.info("on Subscribe ");
                            long x = stats.commandStartTime - stats.createdTime;
                            startTimes.add(x);
                            stats.observedTime = System.currentTimeMillis();
                            long x1 = stats.observedTime - stats.commandStopTime;
                            endTimes.add(x1);
                            count.increment();
                            semaphore.release();
//                        logger.info("on Subscribe "+x+" "+x1+" "+stats);
                        });
                    });
                }
            }).start();
        }



        long lastCount = 0;
        while(count.longValue() < tasks){
            Thread.sleep(1000);

            if (count.longValue()>lastCount) {
                lastCount = count.longValue();
                System.out.println("count = " + lastCount);
                System.out.println("avg start time =" + (startTimes.longValue() / count.longValue()));
                System.out.println("avg end time =" + (endTimes.longValue() / count.longValue()));
            }
        }

        Hystrix.reset();

        System.out.println("avg start time ="+(startTimes.longValue()/count.longValue()));
        System.out.println("avg end time ="+(endTimes.longValue()/count.longValue()));


    }
}
