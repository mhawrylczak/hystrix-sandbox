package mh.sandbox.hystrix.delay;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class TestCommand extends HystrixCommand<Stats> {
    static {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(MdcHystrixConcurrencyStrategy.getInstance());
    }

    private final Logger logger = LoggerFactory.getLogger(TestCommand.class);
    private Stats stats = new Stats();
    protected TestCommand() {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(TestDelay.poolSize)));
    }

    @Override
    protected Stats run() throws Exception {
        return getStats();
    }

    private Stats getStats() {
        stats.commandStartTime = System.currentTimeMillis();
        try {
            Thread.sleep(100 + new Random().nextInt(500));
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        stats.commandStopTime = System.currentTimeMillis();
        return stats;
    }

    @Override
    protected Stats getFallback() {
        return getStats();
    }
}
