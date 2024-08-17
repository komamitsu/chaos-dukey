package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterceptorForFailureTest {
  private Method origin;
  @Mock private Callable<?> callable;

  @BeforeEach
  void setUp() {
    try {
      origin = getClass().getDeclaredMethod("setUp");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void intercept_WithZeroPercentage_ShouldNotThrowException() throws Exception {
    InterceptorForFailure interceptor =
        spy(new InterceptorForFailure(new FailureConfig.Builder().setPercentage(0).build(), true));
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.intercept(origin, callable);
    }
    verify(callable, times(n)).call();
  }

  @Test
  void intercept_WithOneHundredPercentage_ShouldAlwaysThrowException() throws Exception {
    InterceptorForFailure interceptor =
        spy(
            new InterceptorForFailure(
                new FailureConfig.Builder()
                    .setPercentage(100)
                    .setExceptionClassName("java.io.IOException")
                    .build(),
                true));
    int n = 1000;
    for (int i = 0; i < n; i++) {
      assertThrows(IOException.class, () -> interceptor.intercept(origin, callable));
    }
    verify(callable, never()).call();
  }
}
