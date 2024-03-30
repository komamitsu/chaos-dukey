package org.komamitsu.chaosdukey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChaosDukeyInterceptorTest {
  @Mock Callable<?> callable;
  @Captor ArgumentCaptor<Integer> durationArgumentCaptor;

  @Test
  void waitForDuration_GivenArbitraryValue_ShouldWaitProperly() throws InterruptedException {
    ChaosDukeyInterceptor interceptor =
        new ChaosDukeyInterceptor(ChaosDukeyInterceptor.WaitMode.RANDOM, 0, 0);
    {
      long start = System.currentTimeMillis();
      interceptor.waitForDuration(0);
      long duration = System.currentTimeMillis() - start;
      assertTrue(duration >= 0);
      assertTrue(duration < 1000);
    }
    {
      long start = System.currentTimeMillis();
      interceptor.waitForDuration(1000);
      long duration = System.currentTimeMillis() - start;
      assertTrue(duration >= 1000);
      assertTrue(duration < 2000);
    }
  }

  @Test
  void waitForDelay_GivenRandomWaitMode_ShouldRandomlyWait() throws InterruptedException {
    ChaosDukeyInterceptor interceptor =
        spy(new ChaosDukeyInterceptor(ChaosDukeyInterceptor.WaitMode.RANDOM, 100, 100));
    doNothing().when(interceptor).waitForDuration(anyInt());
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.waitForDelay();
    }

    verify(interceptor, times(n)).waitForDuration(durationArgumentCaptor.capture());
    List<Integer> allValues = durationArgumentCaptor.getAllValues();
    assertEquals(n, allValues.size());
    double average = allValues.stream().mapToInt(x -> x).average().getAsDouble();
    assertTrue(average > 20 && average < 80);
  }

  @Test
  void waitForDelay_GivenFixedWaitMode_ShouldFixedlyWait() throws InterruptedException {
    ChaosDukeyInterceptor interceptor =
        spy(new ChaosDukeyInterceptor(ChaosDukeyInterceptor.WaitMode.FIXED, 100, 100));
    doNothing().when(interceptor).waitForDuration(anyInt());
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.waitForDelay();
    }

    verify(interceptor, times(n)).waitForDuration(durationArgumentCaptor.capture());
    List<Integer> allValues = durationArgumentCaptor.getAllValues();
    assertEquals(n, allValues.size());
    double average = allValues.stream().mapToInt(x -> x).average().getAsDouble();
    assertEquals(100, average);
  }

  @ParameterizedTest()
  @EnumSource(ChaosDukeyInterceptor.WaitMode.class)
  void intercept_WithZeroPercentage_ShouldNotWait(ChaosDukeyInterceptor.WaitMode waitMode)
      throws Exception {
    ChaosDukeyInterceptor interceptor = spy(new ChaosDukeyInterceptor(waitMode, 0, 1000));
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.intercept(callable);
    }
    verify(callable, times(n)).call();
    verify(interceptor, never()).waitForDelay();
  }

  @ParameterizedTest()
  @EnumSource(ChaosDukeyInterceptor.WaitMode.class)
  void intercept_WithOneHundredPercentage_ShouldAlwaysWait(ChaosDukeyInterceptor.WaitMode waitMode)
      throws Exception {
    ChaosDukeyInterceptor interceptor = spy(new ChaosDukeyInterceptor(waitMode, 100, 1000));
    doNothing().when(interceptor).waitForDelay();
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.intercept(callable);
    }
    verify(callable, times(n)).call();
    verify(interceptor, times(n)).waitForDelay();
  }
}
