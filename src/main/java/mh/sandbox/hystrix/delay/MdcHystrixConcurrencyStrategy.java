package mh.sandbox.hystrix.delay;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

public class MdcHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {
    private final static MdcHystrixConcurrencyStrategy INSTANCE = new MdcHystrixConcurrencyStrategy();

    public static MdcHystrixConcurrencyStrategy getInstance() {
        return INSTANCE;
    }

    private MdcHystrixConcurrencyStrategy() {

    }


    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        return new MdcAwareCallableWrapper(callable);
    }

    private final static class MdcAwareCallableWrapper<T> implements Callable<T> {

        private Callable<T> wrappedCallable;
        private Map mdc;

        private MdcAwareCallableWrapper(Callable<T> wrappedCallable) {
            this.wrappedCallable = wrappedCallable;
            this.mdc = MDC.getCopyOfContextMap();
        }

        @Override
        public T call() throws Exception {
            Map thisThreadMdc = MDC.getCopyOfContextMap();
            try {
                if (mdc != null) {
                    MDC.setContextMap(mdc);
                }

                return wrappedCallable.call();
            } finally {
                if (thisThreadMdc != null) {
                    MDC.setContextMap(thisThreadMdc);
                } else {
                    MDC.clear();
                }
            }
        }
    }
}
