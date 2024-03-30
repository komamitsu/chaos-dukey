package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.*;

import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

class ChaosDukeyAgentTest {

  @Test
  void parseArguments_GivenEmptyString_ShouldUseDefaultValues() {
    ChaosDukeyAgent agent = ChaosDukeyAgent.parseArguments("");
    assertEquals(ElementMatchers.any(), agent.typeMatcher);
    assertEquals(ElementMatchers.any(), agent.methodMatcher);
    assertEquals(ChaosDukeyAgent.DEFAULT_WAIT_MODE, agent.waitMode);
    assertEquals(ChaosDukeyAgent.DEFAULT_PERCENTAGE, agent.percentage);
    assertEquals(ChaosDukeyAgent.DEFAULT_MAX_DELAY_MILLIS, agent.maxDelayMillis);
    assertEquals(false, agent.debug);
  }

  @Test
  void parseArguments_GivenSpecifiedParametersIncludingRandomWaitMode_ShouldUseThem() {
    ChaosDukeyAgent agent =
        ChaosDukeyAgent.parseArguments(
            "typeNamePattern=typepattern, methodNamePattern = methodpattern ,waitMode=RANDOM,percentage=42,  maxDelayMillis=1234, debug = false");
    assertEquals(ElementMatchers.nameMatches("typepattern"), agent.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("methodpattern"), agent.methodMatcher);
    assertEquals(ChaosDukeyInterceptor.WaitMode.RANDOM, agent.waitMode);
    assertEquals(42, agent.percentage);
    assertEquals(1234, agent.maxDelayMillis);
    assertFalse(agent.debug);
  }

  @Test
  void parseArguments_GivenSpecifiedParametersIncludingFixedWaitMode_ShouldUseThem() {
    ChaosDukeyAgent agent =
        ChaosDukeyAgent.parseArguments(
            "typeNamePattern=typepattern, methodNamePattern = methodpattern ,waitMode=FIXED,percentage=42,  maxDelayMillis=1234, debug = true");
    assertEquals(ElementMatchers.nameMatches("typepattern"), agent.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("methodpattern"), agent.methodMatcher);
    assertEquals(ChaosDukeyInterceptor.WaitMode.FIXED, agent.waitMode);
    assertEquals(42, agent.percentage);
    assertEquals(1234, agent.maxDelayMillis);
    assertTrue(agent.debug);
  }
}
