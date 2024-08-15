package org.komamitsu.chaosdukey;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

class Config {
  static final InterceptorForDelay.DelayWaitMode DEFAULT_WAIT_MODE =
      InterceptorForDelay.DelayWaitMode.RANDOM;
  // 2%
  static final long DEFAULT_PPM = 20000L;
  static final int DEFAULT_MAX_DELAY_MILLIS = 500;

  final boolean debug;
  final DelayConfig delayConfig;
  final FailureConfig failureConfig;

  @Override
  public String toString() {
    return "Config{"
        + "debug="
        + debug
        + ", delayConfig="
        + delayConfig
        + ", failureConfig="
        + failureConfig
        + '}';
  }

  public Config(DelayConfig delayConfig, FailureConfig failureConfig, boolean debug) {
    this.delayConfig = delayConfig;
    this.failureConfig = failureConfig;
    this.debug = debug;
  }

  public static class Builder {
    private DelayConfig delayConfig;
    private FailureConfig failureConfig;
    private boolean debug;

    public Builder setDelayConfig(DelayConfig delayConfig) {
      this.delayConfig = delayConfig;
      return this;
    }

    public Builder setFailureConfig(FailureConfig failureConfig) {
      this.failureConfig = failureConfig;
      return this;
    }

    public Builder setDebug(boolean debug) {
      this.debug = debug;
      return this;
    }

    public Config build() {
      return new Config(delayConfig, failureConfig, debug);
    }
  }

  static class Loader {
    private static final String PROP_NAME_CONFIG_FILE = "configFile";
    private final Map<String, Consumer<String>> propertyHandlers = new HashMap<>();
    private final Config.Builder chaosConfigBuilder = new Config.Builder();
    private final DelayConfig.Builder delayConfigBuilder = new DelayConfig.Builder();
    private final FailureConfig.Builder failureConfigBuilder = new FailureConfig.Builder();

    public Loader() {
      propertyHandlers.put(
          "delay.enabled", v -> delayConfigBuilder.setEnabled(Boolean.parseBoolean(v)));
      propertyHandlers.put(
          "delay.typeNamePattern",
          v -> delayConfigBuilder.setTypeMatcher(ElementMatchers.nameMatches(v)));
      propertyHandlers.put(
          "delay.methodNamePattern",
          v -> delayConfigBuilder.setMethodMatcher(ElementMatchers.nameMatches(v)));
      propertyHandlers.put(
          "delay.waitMode",
          v ->
              delayConfigBuilder.setWaitMode(
                  InterceptorForDelay.DelayWaitMode.valueOf(v.toUpperCase())));
      propertyHandlers.put("delay.ppm", v -> delayConfigBuilder.setPpm(Long.parseLong(v)));
      propertyHandlers.put(
          "delay.percentage", v -> delayConfigBuilder.setPercentage(Integer.parseInt(v)));
      propertyHandlers.put(
          "delay.maxDelayMillis", v -> delayConfigBuilder.setMaxDelayMillis(Integer.parseInt(v)));
      propertyHandlers.put(
          "failure.enabled", v -> failureConfigBuilder.setEnabled(Boolean.parseBoolean(v)));
      propertyHandlers.put(
          "failure.typeNamePattern",
          v -> failureConfigBuilder.setTypeMatcher(ElementMatchers.nameMatches(v)));
      propertyHandlers.put(
          "failure.methodNamePattern",
          v -> failureConfigBuilder.setMethodMatcher(ElementMatchers.nameMatches(v)));
      propertyHandlers.put("failure.ppm", v -> failureConfigBuilder.setPpm(Long.parseLong(v)));
      propertyHandlers.put(
          "failure.percentage", v -> failureConfigBuilder.setPercentage(Integer.parseInt(v)));
      propertyHandlers.put(
          "failure.exceptionClassName", failureConfigBuilder::setExceptionClassName);
      propertyHandlers.put("debug", v -> chaosConfigBuilder.setDebug(Boolean.parseBoolean(v)));
    }

    Config load(Properties origProperties) throws IOException {
      Properties properties;
      String configPath = origProperties.getProperty(PROP_NAME_CONFIG_FILE);
      if (configPath != null) {
        properties = new Properties();
        try (FileInputStream fio = new FileInputStream(configPath)) {
          properties.load(fio);
          properties.remove(PROP_NAME_CONFIG_FILE);
        }
      } else {
        properties = new Properties(origProperties);
      }

      for (String propertyName : properties.stringPropertyNames()) {
        Consumer<String> handler = propertyHandlers.get(propertyName);
        if (handler == null) {
          throw new IllegalArgumentException("Unexpected parameter: " + propertyName);
        }
        handler.accept(properties.getProperty(propertyName));
      }
      return chaosConfigBuilder
          .setDelayConfig(delayConfigBuilder.build())
          .setFailureConfig(failureConfigBuilder.build())
          .build();
    }
  }

  static class DelayConfig {
    // TODO: Remove
    final boolean enabled;
    final ElementMatcher<TypeDefinition> typeMatcher;
    final ElementMatcher<MethodDescription> methodMatcher;
    final InterceptorForDelay.DelayWaitMode waitMode;
    final long ppm;
    final int maxDelayMillis;

    public DelayConfig(
        boolean enabled,
        ElementMatcher<TypeDefinition> typeMatcher,
        ElementMatcher<MethodDescription> methodMatcher,
        InterceptorForDelay.DelayWaitMode waitMode,
        long ppm,
        int maxDelayMillis) {
      this.enabled = enabled;
      this.typeMatcher = typeMatcher;
      this.methodMatcher = methodMatcher;
      this.waitMode = waitMode;
      this.ppm = ppm;
      this.maxDelayMillis = maxDelayMillis;
    }

    public static class Builder {
      private boolean enabled = false;
      private ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.none();
      private ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.none();
      private InterceptorForDelay.DelayWaitMode waitMode = DEFAULT_WAIT_MODE;
      // The default value of this field will be set lazily.
      private Long ppm;
      private Integer percentage;
      private int maxDelayMillis = DEFAULT_MAX_DELAY_MILLIS;

      public Builder setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
      }

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
            ppm = DEFAULT_PPM;
          }
        }
        return new DelayConfig(enabled, typeMatcher, methodMatcher, waitMode, ppm, maxDelayMillis);
      }
    }

    @Override
    public String toString() {
      return "DelayConfig{"
          + "enabled="
          + enabled
          + ", typeMatcher="
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

  static class FailureConfig {
    // TODO: Remove
    final boolean enabled;
    final ElementMatcher<TypeDefinition> typeMatcher;
    final ElementMatcher<MethodDescription> methodMatcher;
    final long ppm;
    final Class<? extends Exception> exceptionClass;

    public FailureConfig(
        boolean enabled,
        ElementMatcher<TypeDefinition> typeMatcher,
        ElementMatcher<MethodDescription> methodMatcher,
        long ppm,
        Class<? extends Exception> exceptionClass) {
      this.enabled = enabled;
      this.typeMatcher = typeMatcher;
      this.methodMatcher = methodMatcher;
      this.ppm = ppm;
      this.exceptionClass = exceptionClass;
    }

    public static class Builder {
      private boolean enabled = false;
      private ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.none();
      private ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.none();
      // The default value of this field will be set lazily.
      private Long ppm;
      private Integer percentage;
      private Class<? extends Exception> exceptionClass = RuntimeException.class;

      public Builder setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
      }

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
            ppm = DEFAULT_PPM;
          }
        }
        return new FailureConfig(enabled, typeMatcher, methodMatcher, ppm, exceptionClass);
      }
    }

    @Override
    public String toString() {
      return "FailureConfig{"
          + "enabled="
          + enabled
          + ", typeMatcher="
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
}
