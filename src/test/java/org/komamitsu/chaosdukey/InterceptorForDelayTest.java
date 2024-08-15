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
class InterceptorForDelayTest {
  @Mock Callable<?> callable;
  @Captor ArgumentCaptor<Integer> durationArgumentCaptor;

  @Test
  void waitForDuration_GivenArbitraryValue_ShouldWaitProperly() throws InterruptedException {
    InterceptorForDelay interceptor =
        new InterceptorForDelay(
            new Config(
                new Config.DelayConfig.Builder().build(),
                new Config.FailureConfig.Builder().build(),
                true));
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
    InterceptorForDelay interceptor =
        spy(
            new InterceptorForDelay(
                new Config(
                    new Config.DelayConfig.Builder()
                        .setEnabled(true)
                        .setWaitMode(InterceptorForDelay.DelayWaitMode.RANDOM)
                        .setMaxDelayMillis(100)
                        .setPercentage(100)
                        .build(),
                    new Config.FailureConfig.Builder().build(),
                    true)));
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
    InterceptorForDelay interceptor =
        spy(
            new InterceptorForDelay(
                new Config(
                    new Config.DelayConfig.Builder()
                        .setEnabled(true)
                        .setWaitMode(InterceptorForDelay.DelayWaitMode.FIXED)
                        .setMaxDelayMillis(100)
                        .setPercentage(100)
                        .build(),
                    new Config.FailureConfig.Builder().build(),
                    true)));
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
  @EnumSource(InterceptorForDelay.DelayWaitMode.class)
  void intercept_WithZeroPercentage_ShouldNotWait(InterceptorForDelay.DelayWaitMode waitMode)
      throws Exception {
    InterceptorForDelay interceptor =
        spy(
            new InterceptorForDelay(
                new Config(
                    new Config.DelayConfig.Builder()
                        .setEnabled(true)
                        .setWaitMode(waitMode)
                        .setMaxDelayMillis(1000)
                        .setPercentage(0)
                        .build(),
                    new Config.FailureConfig.Builder().build(),
                    true)));
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.intercept(callable);
    }
    verify(callable, times(n)).call();
    verify(interceptor, never()).waitForDelay();
  }

  @ParameterizedTest()
  @EnumSource(InterceptorForDelay.DelayWaitMode.class)
  void intercept_WithOneHundredPercentage_ShouldAlwaysWait(
      InterceptorForDelay.DelayWaitMode waitMode) throws Exception {
    InterceptorForDelay interceptor =
        spy(
            new InterceptorForDelay(
                new Config(
                    new Config.DelayConfig.Builder()
                        .setEnabled(true)
                        .setWaitMode(waitMode)
                        .setMaxDelayMillis(1000)
                        .setPercentage(100)
                        .build(),
                    new Config.FailureConfig.Builder().build(),
                    true)));
    doNothing().when(interceptor).waitForDelay();
    int n = 1000;
    for (int i = 0; i < n; i++) {
      interceptor.intercept(callable);
    }
    verify(callable, times(n)).call();
    verify(interceptor, times(n)).waitForDelay();
  }
}
