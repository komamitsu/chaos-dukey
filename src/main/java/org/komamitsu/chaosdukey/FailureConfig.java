package org.komamitsu.chaosdukey;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

class FailureConfig {
  final ElementMatcher<TypeDefinition> typeMatcher;
  final ElementMatcher<MethodDescription> methodMatcher;
  final long ppm;
  final Class<? extends Exception> exceptionClass;

  public FailureConfig(
      ElementMatcher<TypeDefinition> typeMatcher,
      ElementMatcher<MethodDescription> methodMatcher,
      long ppm,
      Class<? extends Exception> exceptionClass) {
    this.typeMatcher = typeMatcher;
    this.methodMatcher = methodMatcher;
    this.ppm = ppm;
    this.exceptionClass = exceptionClass;
  }

  public static class Builder {
    private ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.none();
    private ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.none();
    // The default value of this field will be set lazily.
    private Long ppm;
    private Integer percentage;
    private Class<? extends Exception> exceptionClass = RuntimeException.class;

    public Builder setTypeMatcher(ElementMatcher<TypeDefinition> typeMatcher) {
      this.typeMatcher = typeMatcher;
      return this;
    }

    public Builder setMethodMatcher(ElementMatcher<MethodDescription> methodMatcher) {
      this.methodMatcher = methodMatcher;
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

    public Builder setExceptionClassName(String exceptionClassName) {
      try {
        this.exceptionClass = Class.forName(exceptionClassName).asSubclass(Exception.class);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException(
            String.format("Exception class `%s` is not found", exceptionClassName));
      }
      return this;
    }

    public FailureConfig build() {
      if (ppm == null) {
        if (percentage != null) {
          ppm = percentage * 10000L;
        } else {
          ppm = Config.DEFAULT_PPM;
        }
      }
      return new FailureConfig(typeMatcher, methodMatcher, ppm, exceptionClass);
    }
  }

  @Override
  public String toString() {
    return "FailureConfig{"
        + "typeMatcher="
        + typeMatcher
        + ", methodMatcher="
        + methodMatcher
        + ", ppm="
        + ppm
        + ", exceptionClass="
        + exceptionClass
        + '}';
  }
}
