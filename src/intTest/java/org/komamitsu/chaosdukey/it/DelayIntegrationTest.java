package org.komamitsu.chaosdukey.it;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelayIntegrationTest {
  @Test
  void targetMethod_ShouldBeDelayed() {
      Foo foo = new Foo();
      Instant start = Instant.now();
      assertEquals(3, foo.add(1, 2));
      assertTrue(Duration.between(start, Instant.now()).toMillis() > 1000);
  }

  @Test
  void nonTargetMethod_ShouldNotBeDelayed() {
      Foo foo = new Foo();
      {
          Instant start = Instant.now();
          assertEquals("hello world", foo.concat("hello ", "world"));
          assertTrue(Duration.between(start, Instant.now()).toMillis() < 500);
      }
      Bar bar = new Bar();
      {
          Instant start = Instant.now();
          assertEquals(3, bar.add(1, 2));
          assertTrue(Duration.between(start, Instant.now()).toMillis() < 500);
      }
  }
}
