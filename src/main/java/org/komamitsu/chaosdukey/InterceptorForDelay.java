package org.komamitsu.chaosdukey;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

// This class needs to be public.
public class InterceptorForDelay {
  private final DelayConfig config;
  private final boolean debug;

  enum DelayWaitMode {
    FIXED,
    RANDOM;
  }

  public InterceptorForDelay(DelayConfig config, boolean debug) {
    this.config = config;
    this.debug = debug;
  }

  void waitForDelay() throws InterruptedException {
    int durationMillis;
    switch (config.waitMode) {
      case FIXED:
        durationMillis = config.maxDelayMillis;
        break;
      case RANDOM:
        durationMillis = ThreadLocalRandom.current().nextInt(config.maxDelayMillis) + 1;
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
  public Object intercept(@Origin Method origin, @SuperCall Callable<?> callable) throws Exception {
    if (ThreadLocalRandom.current().nextLong(1000000) < config.ppm) {
      boolean delayBeforeInvocation = ThreadLocalRandom.current().nextBoolean();

      if (delayBeforeInvocation) {
        if (debug) {
          System.err.printf(
              "[Chaos-Dukey] Waiting before the target method invocation in `%s`.\n", origin);
        }
        waitForDelay();
      }

      Object result = callable.call();

      if (!delayBeforeInvocation) {
        if (debug) {
          System.err.printf(
              "[Chaos-Dukey] Waiting after the target method invocation in `%s`.\n", origin);
        }
        waitForDelay();
      }

      return result;
    } else {
      return callable.call();
    }
  }
}
