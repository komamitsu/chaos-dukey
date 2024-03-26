package org.komamitsu.chaosdukey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChaosDukeyInterceptorTest {
    @Mock
    Callable<?> callable;

    @Test
    void intercept_WithZeroPercentage_ShouldNotWait() throws Exception {
        ChaosDukeyInterceptor interceptor = spy(new ChaosDukeyInterceptor(0, 1000));
        int n = 1000;
        for (int i = 0; i < n; i++) {
            interceptor.intercept(callable);
        }
        verify(callable, times(n)).call();
        verify(interceptor, never()).waitRandomMillis();
    }

    @Test
    void intercept_WithOneHundredPercentage_ShouldAlwaysWait() throws Exception {
        ChaosDukeyInterceptor interceptor = spy(new ChaosDukeyInterceptor(100, 1000));
        doNothing().when(interceptor).waitRandomMillis();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            interceptor.intercept(callable);
        }
        verify(callable, times(n)).call();
        verify(interceptor, times(n)).waitRandomMillis();
    }
}