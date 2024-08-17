package org.komamitsu.chaosdukey;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

// This class needs to be public.
public class InterceptorForFailure {
  private final FailureConfig config;
  private final boolean debug;
  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  public InterceptorForFailure(FailureConfig config, boolean debug) {
    this.config = config;
    this.debug = debug;
  }

  @RuntimeType
  public Object intercept(@SuperCall Callable<?> callable) throws Exception {
    if (random.nextLong(1000000) < config.ppm) {
      throw config.exceptionClass.newInstance();
    } else {
      return callable.call();
    }
  }
}
