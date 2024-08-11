package org.komamitsu.chaosdukey;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

class Config {
  static final Interceptor.WaitMode DEFAULT_WAIT_MODE =
          Interceptor.WaitMode.RANDOM;
  // 2%
  static final long DEFAULT_PPM = 20000L;
  static final int DEFAULT_MAX_DELAY_MILLIS = 500;

  final boolean debug;
  final DelayConfig delayConfig;

  public Config(DelayConfig delayConfig, boolean debug) {
    this.delayConfig = delayConfig;
    this.debug = debug;
  }

  public static class Builder {
    private DelayConfig delayConfig;
    private boolean debug;

    public Builder setDelayConfig(DelayConfig delayConfig) {
      this.delayConfig = delayConfig;
      return this;
    }

    public Builder setDebug(boolean debug) {
      this.debug = debug;
      return this;
    }

    public Config build() {
      return new Config(delayConfig, debug);
    }
  }
  static class Loader {
    private static final String PROP_NAME_CONFIG_PATH = "configPath";
    private final Map<String, Consumer<String>> propertyHandlers = new HashMap<>();
    private final Config.Builder chaosConfigBuilder = new Config.Builder();
    private final DelayConfig.Builder delayConfigBuilder = new DelayConfig.Builder();

    public Loader() {
      propertyHandlers.put("delay.enabled", v -> delayConfigBuilder.setEnabled(Boolean.parseBoolean(v)));
      propertyHandlers.put("delay.typeNamePattern", v -> delayConfigBuilder.setTypeMatcher(ElementMatchers.nameMatches(v)));
      propertyHandlers.put("delay.methodNamePattern", v -> delayConfigBuilder.setMethodMatcher(ElementMatchers.nameMatches(v)));
      propertyHandlers.put("delay.waitMode", v -> delayConfigBuilder.setWaitMode(Interceptor.WaitMode.valueOf(v.toUpperCase())));
      propertyHandlers.put("delay.ppm", v -> delayConfigBuilder.setPpm(Long.parseLong(v)));
      propertyHandlers.put("delay.percentage", v -> delayConfigBuilder.setPercentage(Integer.parseInt(v)));
      propertyHandlers.put("delay.maxDelayMillis", v -> delayConfigBuilder.setMaxDelayMillis(Integer.parseInt(v)));
      propertyHandlers.put("debug", v -> chaosConfigBuilder.setDebug(Boolean.parseBoolean(v)));
    }

    Config load(Properties origProperties) throws IOException {
      Properties properties;
      String configPath = origProperties.getProperty(PROP_NAME_CONFIG_PATH);
      if (configPath != null) {
        properties = new Properties();
        try (FileInputStream fio = new FileInputStream(configPath)) {
          properties.load(fio);
          properties.remove(PROP_NAME_CONFIG_PATH);
        }
      }
      else {
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
              .build();
    }
  }

  static class DelayConfig {
    final boolean enabled;
    final ElementMatcher<TypeDefinition> typeMatcher;
    final ElementMatcher<MethodDescription> methodMatcher;
    final Interceptor.WaitMode waitMode;
    final long ppm;
    final int maxDelayMillis;

    public DelayConfig(
            boolean enabled,
            ElementMatcher<TypeDefinition> typeMatcher,
            ElementMatcher<MethodDescription> methodMatcher,
            Interceptor.WaitMode waitMode,
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
      private ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.any();
      private ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.any();
      private Interceptor.WaitMode waitMode = DEFAULT_WAIT_MODE;
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

      public Builder setWaitMode(Interceptor.WaitMode waitMode) {
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
          }
          else {
            ppm = DEFAULT_PPM;
          }
        }
        return new DelayConfig(enabled, typeMatcher, methodMatcher, waitMode, ppm, maxDelayMillis);
      }
    }
  }
}
