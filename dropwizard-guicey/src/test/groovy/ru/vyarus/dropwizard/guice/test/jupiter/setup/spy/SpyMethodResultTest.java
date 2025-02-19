package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

/**
 * @author Vyacheslav Rusakov
 * @since 18.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class SpyMethodResultTest {

    @SpyBean
    Service spy;

    @Test
    void testResultCapture() {
        ResultCaptor<String> resultCaptor = new ResultCaptor<>();
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.doAnswer(resultCaptor).when(spy).foo(argumentCaptor.capture());

        // call method
        Assertions.assertThat(spy.foo(11)).isEqualTo("foo11");
        // result captured
        Assertions.assertThat(resultCaptor.getResult()).isEqualTo("foo11");
        Assertions.assertThat(argumentCaptor.getValue()).isEqualTo(11);

        Mockito.verify(spy, Mockito.times(1)).foo(11);
    }

    public static class ResultCaptor<T> implements Answer {
        private T result = null;
        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }

    public static class Service {
        public String foo(int i) {
            return "foo" + i;
        }
    }
}
