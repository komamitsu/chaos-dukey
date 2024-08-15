package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Properties;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

class ConfigTest {
  @Test
  void load_GivenEmptyString_ShouldUseDefaultValues() throws IOException {
    Properties properties = new Properties();

    Config config = new Config.Loader().load(properties);
    assertEquals(ElementMatchers.none(), config.delayConfig.typeMatcher);
    assertEquals(ElementMatchers.none(), config.delayConfig.methodMatcher);
    assertEquals(InterceptorForDelay.DelayWaitMode.RANDOM, config.delayConfig.waitMode);
    assertEquals(20000L, config.delayConfig.ppm);
    assertEquals(500, config.delayConfig.maxDelayMillis);
    assertEquals(ElementMatchers.none(), config.failureConfig.typeMatcher);
    assertEquals(ElementMatchers.none(), config.failureConfig.methodMatcher);
    assertEquals(20000L, config.failureConfig.ppm);
    assertEquals(RuntimeException.class, config.failureConfig.exceptionClass);
    assertFalse(config.debug);
  }

  @Test
  void load_GivenSpecifiedParametersIncludingRandomWaitModeAndPercentage_ShouldUseThem()
      throws IOException {
    Properties properties = new Properties();
    properties.put("delay.typeNamePattern", "^abc.def.MyClass$");
    properties.put("delay.methodNamePattern", "^myMethod$");
    properties.put("delay.waitMode", "Random");
    properties.put("delay.percentage", "42");
    properties.put("delay.maxDelayMillis", "1234");
    properties.put("failure.typeNamePattern", "^xyz.vw.YourClass$");
    properties.put("failure.methodNamePattern", "^yourMethod$");
    properties.put("failure.percentage", "31");
    properties.put("failure.exceptionClassName", "java.io.IOException");
    properties.put("debug", "false");

    Config config = new Config.Loader().load(properties);
    assertEquals(ElementMatchers.nameMatches("^abc.def.MyClass$"), config.delayConfig.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("^myMethod$"), config.delayConfig.methodMatcher);
    assertEquals(InterceptorForDelay.DelayWaitMode.RANDOM, config.delayConfig.waitMode);
    assertEquals(420000, config.delayConfig.ppm);
    assertEquals(1234, config.delayConfig.maxDelayMillis);
    assertEquals(
        ElementMatchers.nameMatches("^xyz.vw.YourClass$"), config.failureConfig.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("^yourMethod$"), config.failureConfig.methodMatcher);
    assertEquals(310000, config.failureConfig.ppm);
    assertEquals(IOException.class, config.failureConfig.exceptionClass);
    assertFalse(config.debug);
  }

  @Test
  void load_GivenSpecifiedParametersIncludingFixedWaitModeAndPpmAndRegexpPatterns_ShouldUseThem()
      throws IOException {
    Properties properties = new Properties();
    properties.put("delay.typeNamePattern", "^(?:abc.def.MyClass|xyz.vw.(?:Aaa.*|Bbb))$");
    properties.put("delay.methodNamePattern", "^(?:my(?:Method|Function)|yourMethod)$");
    properties.put("delay.waitMode", "FIXED");
    properties.put("delay.ppm", "42");
    properties.put("delay.maxDelayMillis", "1234");
    properties.put("failure.typeNamePattern", "^(?:abc.def.YourClass|xyz.vw.(?:Ccc.*|Ddd))$");
    properties.put("failure.methodNamePattern", "^(?:his(?:Method|Function)|herMethod)$");
    properties.put("failure.ppm", "31");
    properties.put("failure.exceptionClassName", "java.io.IOException");
    properties.put("debug", "true");

    Config config = new Config.Loader().load(properties);
    assertEquals(
        ElementMatchers.nameMatches("^(?:abc.def.MyClass|xyz.vw.(?:Aaa.*|Bbb))$"),
        config.delayConfig.typeMatcher);
    assertEquals(
        ElementMatchers.nameMatches("^(?:my(?:Method|Function)|yourMethod)$"),
        config.delayConfig.methodMatcher);
    assertEquals(InterceptorForDelay.DelayWaitMode.FIXED, config.delayConfig.waitMode);
    assertEquals(42, config.delayConfig.ppm);
    assertEquals(1234, config.delayConfig.maxDelayMillis);
    assertEquals(
        ElementMatchers.nameMatches("^(?:abc.def.YourClass|xyz.vw.(?:Ccc.*|Ddd))$"),
        config.failureConfig.typeMatcher);
    assertEquals(
        ElementMatchers.nameMatches("^(?:his(?:Method|Function)|herMethod)$"),
        config.failureConfig.methodMatcher);
    assertEquals(31, config.failureConfig.ppm);
    assertEquals(IOException.class, config.failureConfig.exceptionClass);
    assertTrue(config.debug);
  }

  @Test
  void load_GivenBothPpmAndPercentageInDelayConfig_ShouldThrowException() {
    Properties properties = new Properties();
    properties.put("delay.ppm", "42");
    properties.put("delay.percentage", "8");

    assertThrows(IllegalArgumentException.class, () -> new Config.Loader().load(properties));
  }

  @Test
  void load_GivenBothPpmAndPercentageInFailureConfig_ShouldThrowException() {
    Properties properties = new Properties();
    properties.put("failure.ppm", "42");
    properties.put("failure.percentage", "8");

    assertThrows(IllegalArgumentException.class, () -> new Config.Loader().load(properties));
  }
}
