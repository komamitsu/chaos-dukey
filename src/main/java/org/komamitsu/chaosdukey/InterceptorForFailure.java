package org.komamitsu.chaosdukey;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

// This class needs to be public.
public class InterceptorForFailure {
  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  private final Config config;

  public InterceptorForFailure(Config config) {
    this.config = config;
  }

  @RuntimeType
  public Object intercept(@SuperCall Callable<?> callable) throws Exception {
    if (config.failureConfig.enabled && random.nextLong(1000000) < config.failureConfig.ppm) {
      throw config.failureConfig.exceptionClass.newInstance();
    } else {
      return callable.call();
    }
  }
}
