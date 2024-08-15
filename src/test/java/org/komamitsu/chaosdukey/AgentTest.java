package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class AgentTest {
  @Test
  void propertiesFromArguments() {
    Properties properties =
        Agent.propertiesFromArguments(
            "delay.typeNamePattern=^org\\.example\\.transaction\\.(?:Foo|Bar)$, delay.maxDelayMillis = 250 , failure.methodNamePattern=^unstableMethod$");
    assertEquals(
        "^org\\.example\\.transaction\\.(?:Foo|Bar)$",
        properties.getProperty("delay.typeNamePattern"));
    assertEquals("250", properties.getProperty("delay.maxDelayMillis"));
    assertEquals("^unstableMethod$", properties.getProperty("failure.methodNamePattern"));
  }
}
