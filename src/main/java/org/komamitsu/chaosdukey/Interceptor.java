package org.komamitsu.chaosdukey;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

// This class needs to be public.
public class Interceptor {
  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  private final Config config;

  enum WaitMode {
    FIXED,
    RANDOM;
  }

  public Interceptor(Config config) {
    this.config = config;
  }

  void waitForDelay() throws InterruptedException {
    int durationMillis;
    switch (config.delayConfig.waitMode) {
      case FIXED:
        durationMillis = config.delayConfig.maxDelayMillis;
        break;
      case RANDOM:
        durationMillis = random.nextInt(config.delayConfig.maxDelayMillis) + 1;
        break;
      default:
        throw new AssertionError("Shouldn't reach here");
    }
    waitForDuration(durationMillis);
  }

  void waitForDuration(int durationMillis) throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(durationMillis);
  }

  @RuntimeType
  public Object intercept(@SuperCall Callable<?> callable) throws Exception {
    if (random.nextLong(1000000) < config.delayConfig.ppm) {
      boolean delayBeforeInvocation = random.nextBoolean();
      if (delayBeforeInvocation) {
        if (config.debug) {
          System.err.println("[Chaos-Dukey] Waiting before the target method invocation.");
        }
        waitForDelay();
      }

      Object result = callable.call();

      if (!delayBeforeInvocation) {
        if (config.debug) {
          System.err.println("[Chaos-Dukey] Waiting after the target method invocation.");
        }
        waitForDelay();
      }

      return result;
    } else {
      return callable.call();
    }
  }
}
