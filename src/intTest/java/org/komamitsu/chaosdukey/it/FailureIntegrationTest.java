package org.komamitsu.chaosdukey.it;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FailureIntegrationTest {
  @Test
  void test() {
      Bar bar = new Bar();
      {
          assertEquals(3, bar.add(1, 2));
      }
      {
          assertThrows(IOException.class, () -> assertEquals("hello world", bar.concat("hello ", "world")));
      }
  }
}
