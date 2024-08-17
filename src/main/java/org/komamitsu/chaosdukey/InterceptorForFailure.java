package org.komamitsu.chaosdukey;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

// This class needs to be public.
public class InterceptorForFailure {
  private final FailureConfig config;
  private final boolean debug;

  public InterceptorForFailure(FailureConfig config, boolean debug) {
    this.config = config;
    this.debug = debug;
  }

  @RuntimeType
  public Object intercept(@Origin Method origin, @SuperCall Callable<?> callable) throws Exception {
    if (ThreadLocalRandom.current().nextLong(1000000) < config.ppm) {
      boolean delayBeforeInvocation = ThreadLocalRandom.current().nextBoolean();
      if (delayBeforeInvocation) {
        if (debug) {
          System.err.printf(
              "[Chaos-Dukey] Throwing an exception before the target method invocation in `%s`.\n",
              origin);
        }
        throw config.exceptionClass.newInstance();
      }

      callable.call();

      if (debug) {
        System.err.printf(
            "[Chaos-Dukey] Throwing an exception after the target method invocation in `%s`.\n",
            origin);
      }
      throw config.exceptionClass.newInstance();
    } else {
      return callable.call();
    }
  }
}
