package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.*;

import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

class ChaosDukeyConfigTest {

  @Test
  void parseArguments_GivenEmptyString_ShouldUseDefaultValues() throws IOException {
    Properties properties = new Properties();
    ChaosDukeyConfig config = new ChaosDukeyConfig.Loader().load(properties);
    assertFalse(config.delayConfig.enabled);
    assertEquals(ElementMatchers.any(), config.delayConfig.typeMatcher);
    assertEquals(ElementMatchers.any(), config.delayConfig.methodMatcher);
    assertEquals(ChaosDukeyInterceptor.WaitMode.RANDOM, config.delayConfig.waitMode);
    assertEquals(20000L, config.delayConfig.ppm);
    assertEquals(500, config.delayConfig.maxDelayMillis);
    assertFalse(config.debug);
  }

  @Test
  void parseArguments_GivenSpecifiedParametersIncludingRandomWaitModeAndPercentage_ShouldUseThem() throws IOException {
    Properties properties = new Properties();
    properties.put("delay.enabled", "true");
    properties.put("delay.typeNamePattern", "^abc.def.MyClass$");
    properties.put("delay.methodNamePattern", "^myMethod$");
    properties.put("delay.waitMode", "Random");
    properties.put("delay.percentage", "42");
    properties.put("delay.maxDelayMillis", "1234");
    properties.put("debug", "false");

    ChaosDukeyConfig config = new ChaosDukeyConfig.Loader().load(properties);
    assertTrue(config.delayConfig.enabled);
    assertEquals(ElementMatchers.nameMatches("^abc.def.MyClass$"), config.delayConfig.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("^myMethod$"), config.delayConfig.methodMatcher);
    assertEquals(ChaosDukeyInterceptor.WaitMode.RANDOM, config.delayConfig.waitMode);
    assertEquals(420000, config.delayConfig.ppm);
    assertEquals(1234, config.delayConfig.maxDelayMillis);
    assertFalse(config.debug);
  }

  @Test
  void parseArguments_GivenSpecifiedParametersIncludingFixedWaitModeAndPpmAndRegexpPatterns_ShouldUseThem() throws IOException {
    Properties properties = new Properties();
    properties.put("delay.enabled", "true");
    properties.put("delay.typeNamePattern", "^(?:abc.def.MyClass|xyz.vw.(?:Aaa.*|Bbb))$");
    properties.put("delay.methodNamePattern", "^(?:my(?:Method|Function)|yourMethod)$");
    properties.put("delay.waitMode", "FIXED");
    properties.put("delay.ppm", "42");
    properties.put("delay.maxDelayMillis", "1234");
    properties.put("debug", "true");

    ChaosDukeyConfig config = new ChaosDukeyConfig.Loader().load(properties);
    assertTrue(config.delayConfig.enabled);
    assertEquals(ElementMatchers.nameMatches("^(?:abc.def.MyClass|xyz.vw.(?:Aaa.*|Bbb))$"), config.delayConfig.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("^(?:my(?:Method|Function)|yourMethod)$"), config.delayConfig.methodMatcher);
    assertEquals(ChaosDukeyInterceptor.WaitMode.FIXED, config.delayConfig.waitMode);
    assertEquals(42, config.delayConfig.ppm);
    assertEquals(1234, config.delayConfig.maxDelayMillis);
    assertTrue(config.debug);
  }


  @Test
  void parseArguments_GivenBothPpmAndPercentage_ShouldThrowException() {
    Properties properties = new Properties();
    properties.put("delay.enabled", "true");
    properties.put("delay.ppm", "42");
    properties.put("delay.percentage", "8");

    assertThrows(IllegalArgumentException.class,
            () -> new ChaosDukeyConfig.Loader().load(properties));
  }
}
