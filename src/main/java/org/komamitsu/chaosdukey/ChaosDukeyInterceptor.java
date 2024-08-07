package org.komamitsu.chaosdukey;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

// This class needs to be public.
public class ChaosDukeyInterceptor {
  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  private final WaitMode waitMode;
  private final long ppm;
  private final int maxDelayMillis;
  private final boolean debug;

  enum WaitMode {
    FIXED,
    RANDOM;
  }

  public ChaosDukeyInterceptor(WaitMode waitMode, long ppm, int maxDelayMillis, boolean debug) {
    this.waitMode = waitMode;
    this.ppm = ppm;
    this.maxDelayMillis = maxDelayMillis;
    this.debug = debug;
  }

  void waitForDelay() throws InterruptedException {
    int durationMillis;
    switch (waitMode) {
      case FIXED:
        durationMillis = maxDelayMillis;
        break;
      case RANDOM:
        durationMillis = random.nextInt(maxDelayMillis) + 1;
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
    if (random.nextLong(1000000) < ppm) {
      boolean delayBeforeInvocation = random.nextBoolean();
      if (delayBeforeInvocation) {
        if (debug) {
          System.err.println("[Chaos-Dukey] Waiting before the target method invocation.");
        }
        waitForDelay();
      }

      Object result = callable.call();

      if (!delayBeforeInvocation) {
        if (debug) {
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
