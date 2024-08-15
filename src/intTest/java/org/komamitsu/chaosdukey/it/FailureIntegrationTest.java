package org.komamitsu.chaosdukey.it;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FailureIntegrationTest {
    @Test
    void targetMethod_ShouldThrowException() {
        Bar bar = new Bar();
        assertThrows(IOException.class, () -> bar.concat("hello ", "world"));
    }

    @Test
    void nonTargetMethod_ShouldNotThrowException() {
        Foo foo = new Foo();
        assertEquals("hello world", foo.concat("hello ", "world"));

        Bar bar = new Bar();
        assertEquals(3, bar.add(1, 2));
    }
}
