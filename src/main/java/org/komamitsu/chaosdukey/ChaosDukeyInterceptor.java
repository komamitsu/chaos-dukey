package org.komamitsu.chaosdukey;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ChaosDukeyInterceptor {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private final int percentage;
    private final int maxDelayMillis;

    public ChaosDukeyInterceptor(int percentage, int maxDelayMillis) {
        this.percentage = percentage;
        this.maxDelayMillis = maxDelayMillis;
    }

    void waitRandomMillis() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(random.nextInt(maxDelayMillis));
    }

    @RuntimeType
    public Object intercept(@SuperCall Callable<?> callable) throws Exception {
        if (random.nextInt(100) < percentage) {
            boolean delayBeforeInvocation = random.nextBoolean();
            if (delayBeforeInvocation) {
                waitRandomMillis();
            }

            Object result = callable.call();

            if (!delayBeforeInvocation) {
                waitRandomMillis();
            }

            return result;
        }
        else {
            return callable.call();
        }
    }
}
