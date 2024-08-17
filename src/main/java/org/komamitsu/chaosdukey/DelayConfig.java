package org.komamitsu.chaosdukey;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

class DelayConfig {
  final ElementMatcher<TypeDefinition> typeMatcher;
  final ElementMatcher<MethodDescription> methodMatcher;
  final InterceptorForDelay.DelayWaitMode waitMode;
  final long ppm;
  final int maxDelayMillis;

  public DelayConfig(
      ElementMatcher<TypeDefinition> typeMatcher,
      ElementMatcher<MethodDescription> methodMatcher,
      InterceptorForDelay.DelayWaitMode waitMode,
      long ppm,
      int maxDelayMillis) {
    this.typeMatcher = typeMatcher;
    this.methodMatcher = methodMatcher;
    this.waitMode = waitMode;
    this.ppm = ppm;
    this.maxDelayMillis = maxDelayMillis;
  }

  public static class Builder {
    private ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.none();
    private ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.none();
    private InterceptorForDelay.DelayWaitMode waitMode = Config.DEFAULT_WAIT_MODE;
    // The default value of this field will be set lazily.
    private Long ppm;
    private Integer percentage;
    private int maxDelayMillis = Config.DEFAULT_MAX_DELAY_MILLIS;

    public Builder setTypeMatcher(ElementMatcher<TypeDefinition> typeMatcher) {
      this.typeMatcher = typeMatcher;
      return this;
    }

    public Builder setMethodMatcher(ElementMatcher<MethodDescription> methodMatcher) {
      this.methodMatcher = methodMatcher;
      return this;
    }

    public Builder setWaitMode(InterceptorForDelay.DelayWaitMode waitMode) {
      this.waitMode = waitMode;
      return this;
    }

    public Builder setPpm(long ppm) {
      if (percentage != null) {
        throw new IllegalArgumentException("`ppm` cannot be specified with `percentage`");
      }
      this.ppm = ppm;
      return this;
    }

    public Builder setPercentage(int percentage) {
      if (ppm != null) {
        throw new IllegalArgumentException("`percentage` cannot be specified with `ppm`");
      }
      this.percentage = percentage;
      return this;
    }

    public Builder setMaxDelayMillis(int maxDelayMillis) {
      this.maxDelayMillis = maxDelayMillis;
      return this;
    }

    public DelayConfig build() {
      if (ppm == null) {
        if (percentage != null) {
          ppm = percentage * 10000L;
        } else {
          ppm = Config.DEFAULT_PPM;
        }
      }
      return new DelayConfig(typeMatcher, methodMatcher, waitMode, ppm, maxDelayMillis);
    }
  }

  @Override
  public String toString() {
    return "DelayConfig{"
        + "typeMatcher="
        + typeMatcher
        + ", methodMatcher="
        + methodMatcher
        + ", waitMode="
        + waitMode
        + ", ppm="
        + ppm
        + ", maxDelayMillis="
        + maxDelayMillis
        + '}';
  }
}
