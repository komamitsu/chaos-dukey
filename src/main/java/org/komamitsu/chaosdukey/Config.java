package org.komamitsu.chaosdukey;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
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
}
