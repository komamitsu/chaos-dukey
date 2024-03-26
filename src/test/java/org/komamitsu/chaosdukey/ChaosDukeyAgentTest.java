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
    assertEquals(ChaosDukeyAgent.DEFAULT_PERCENTAGE, agent.percentage);
    assertEquals(ChaosDukeyAgent.DEFAULT_MAX_DELAY_MILLIS, agent.maxDelayMillis);
  }

  @Test
  void parseArguments_GivenSpecifiedParameters_ShouldUseThem() {
    ChaosDukeyAgent agent =
        ChaosDukeyAgent.parseArguments(
            "typeNamePattern=typepattern, methodNamePattern = methodpattern ,percentage=42,  maxDelayMillis=1234");
    assertEquals(ElementMatchers.nameMatches("typepattern"), agent.typeMatcher);
    assertEquals(ElementMatchers.nameMatches("methodpattern"), agent.methodMatcher);
    assertEquals(42, agent.percentage);
    assertEquals(1234, agent.maxDelayMillis);
  }
}
