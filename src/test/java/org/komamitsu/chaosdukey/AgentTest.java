package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

class AgentTest {
  @Test
  void configFromArguments() throws IOException {
    Config config =
        Agent.configFromArguments(
            "delay.enabled=true, delay.typeNamePattern=^org\\.example\\.transaction\\.(?:Foo|Bar)$, delay.maxDelayMillis = 250 ");
    assertTrue(config.delayConfig.enabled);
    assertEquals(
        ElementMatchers.nameMatches("^org\\.example\\.transaction\\.(?:Foo|Bar)$"),
        config.delayConfig.typeMatcher);
    assertEquals(250, config.delayConfig.maxDelayMillis);
  }
}
