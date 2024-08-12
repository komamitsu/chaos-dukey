package org.komamitsu.chaosdukey.it;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DelayIntegrationTest {
  @Test
  void test() {
      Foo foo = new Foo();
      {
          Instant start = Instant.now();
          assertEquals(3, foo.add(1, 2));
          assertTrue(Duration.between(start, Instant.now()).toMillis() > 1000);
      }
      {
          Instant start = Instant.now();
          assertEquals("hello world", foo.concat("hello ", "world"));
          assertTrue(Duration.between(start, Instant.now()).toMillis() < 500);
      }
  }
}
