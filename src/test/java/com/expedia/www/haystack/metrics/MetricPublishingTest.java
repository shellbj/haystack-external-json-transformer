package com.expedia.www.haystack.metrics;

import com.netflix.servo.publish.AsyncMetricObserver;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.expedia.www.haystack.metrics.MetricPublishing.ASYNC_METRIC_OBSERVER_NAME;
import static com.expedia.www.haystack.metrics.MetricPublishing.POLL_INTERVAL_SECONDS_TO_EXPIRE_TIME_MULTIPLIER;
import static com.expedia.www.haystack.metrics.MetricPublishing.POLL_INTERVAL_SECONDS_TO_HEARTBEAT_MULTIPLIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricPublishingTest {
    private static final Random RANDOM = new Random();
    private static final int POLL_INTERVAL_SECONDS = RANDOM.nextInt(Short.MAX_VALUE);
    private static final int QUEUE_SIZE = RANDOM.nextInt(Byte.MAX_VALUE) + 1;
    private static final long EXPIRE_TIME = POLL_INTERVAL_SECONDS_TO_EXPIRE_TIME_MULTIPLIER * POLL_INTERVAL_SECONDS;
    private static final long HEARTBEAT = POLL_INTERVAL_SECONDS_TO_HEARTBEAT_MULTIPLIER * POLL_INTERVAL_SECONDS;
    private static final String ADDRESS = String.format("%d.%d.%d.%d", RANDOM.nextInt(Byte.MAX_VALUE),
            RANDOM.nextInt(Byte.MAX_VALUE), RANDOM.nextInt(Byte.MAX_VALUE), RANDOM.nextInt(Byte.MAX_VALUE));
    private static final String PREFIX = RANDOM.nextLong() + "PREFIX";
    private static final int PORT = RANDOM.nextInt(Short.MAX_VALUE);
    private static final String ADDRESS_AND_PORT = ADDRESS + ':' + PORT;

    @Mock
    private MetricPublishing.Factory mockFactory;
    private MetricPublishing.Factory realFactory;

    @Mock
    private MetricObserver mockMetricObserver;

    @Mock
    private GraphiteConfig mockGraphiteConfig;
    private GraphiteConfig realGraphiteConfig;

    @Mock
    private MetricObserver mockAsyncMetricObserver;

    @Mock
    private MetricObserver mockCounterToRateMetricTransform;

    @Mock
    private MetricObserver mockGraphiteMetricObserver;

    @Mock
    private PollRunnable mockTask;

    @Mock
    private MetricPoller mockMetricPoller;

    @Before
    public void setUp() {
        realFactory = MetricPublishing.factory;
        realGraphiteConfig = MetricPublishing.graphiteConfig;
        MetricPublishing.factory = mockFactory;
        MetricPublishing.graphiteConfig = mockGraphiteConfig;
    }

    @After
    public void tearDown() {
        MetricPublishing.factory = realFactory;
        MetricPublishing.graphiteConfig = realGraphiteConfig;
        if(PollScheduler.getInstance().isStarted()) {
            PollScheduler.getInstance().stop();
        }
        verifyNoMoreInteractions(mockFactory, mockMetricObserver, mockGraphiteConfig, mockAsyncMetricObserver,
                mockCounterToRateMetricTransform, mockGraphiteMetricObserver, mockTask, mockMetricPoller);
    }

    @Test
    public void testStart() throws UnknownHostException {
        final List<MetricObserver> observers = whensForStart();

        MetricPublishing.start();

        verifiesForStart(observers);
    }

    private List<MetricObserver> whensForStart() {
        whensForCreateGraphiteObserver();
        when(mockFactory.createMonitorRegistryMetricPoller()).thenReturn(mockMetricPoller);
        when(mockFactory.createTask(any(MetricPoller.class), anyListOf(MetricObserver.class))).thenReturn(mockTask);
        return Collections.singletonList(mockCounterToRateMetricTransform);
    }

    private void verifiesForStart(List<MetricObserver> observers) throws UnknownHostException {
        verifiesForCreateGraphiteObserver(3);
        verify(mockFactory).createMonitorRegistryMetricPoller();
        verify(mockFactory).createTask(mockMetricPoller, observers);
        verify(mockTask).run();
    }

    @Test
    public void testCreateGraphiteObserver() throws UnknownHostException {
        whensForCreateGraphiteObserver();

        final MetricObserver metricObserver = MetricPublishing.createGraphiteObserver();
        assertSame(mockCounterToRateMetricTransform, metricObserver);

        verifiesForCreateGraphiteObserver(2);
    }

    private void whensForCreateGraphiteObserver() {
        whensForAsync();
        whensForRateTransform();
        when(mockGraphiteConfig.address()).thenReturn(ADDRESS);
        when(mockGraphiteConfig.port()).thenReturn(PORT);
        when(mockFactory.createGraphiteMetricObserver(anyString(), anyString())).thenReturn(mockGraphiteMetricObserver);
    }

    private void verifiesForCreateGraphiteObserver(int pollIntervalSecondsTimes) throws UnknownHostException {
        verifiesForAsync(pollIntervalSecondsTimes, mockGraphiteMetricObserver);
        verifiesForRateTransform(pollIntervalSecondsTimes, mockAsyncMetricObserver);
        verify(mockGraphiteConfig).address();
        verify(mockGraphiteConfig).port();
        verify(mockFactory).createGraphiteMetricObserver(ASYNC_METRIC_OBSERVER_NAME, ADDRESS_AND_PORT);
    }

    @Test
    public void testRateTransform() {
        whensForRateTransform();

        final MetricObserver metricObserver = MetricPublishing.rateTransform(mockMetricObserver);
        assertSame(mockCounterToRateMetricTransform, metricObserver);

        verifiesForRateTransform(1, mockMetricObserver);
    }

    private void whensForRateTransform() {
        when(mockFactory.createCounterToRateMetricTransform(any(MetricObserver.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockCounterToRateMetricTransform);
        when(mockGraphiteConfig.pollIntervalSeconds()).thenReturn(POLL_INTERVAL_SECONDS);
    }

    private void verifiesForRateTransform(int pollIntervalSecondsTimes, MetricObserver metricObserver) {
        verify(mockFactory).createCounterToRateMetricTransform(metricObserver, HEARTBEAT, TimeUnit.SECONDS);
        verify(mockGraphiteConfig, times(pollIntervalSecondsTimes)).pollIntervalSeconds();
    }

    @Test
    public void testAsync() {
        whensForAsync();

        final MetricObserver metricObserver = MetricPublishing.async(mockMetricObserver);
        assertSame(mockAsyncMetricObserver, metricObserver);

        verifiesForAsync(1, mockMetricObserver);
    }

    private void whensForAsync() {
        when(mockGraphiteConfig.pollIntervalSeconds()).thenReturn(POLL_INTERVAL_SECONDS);
        when(mockGraphiteConfig.queueSize()).thenReturn(QUEUE_SIZE);
        when(mockFactory.createAsyncMetricObserver(any(MetricObserver.class), anyInt(), anyLong()))
                .thenReturn(mockAsyncMetricObserver);
    }

    private void verifiesForAsync(int pollIntervalSecondsTimes, MetricObserver metricObserver) {
        verify(mockGraphiteConfig, times(pollIntervalSecondsTimes)).pollIntervalSeconds();
        verify(mockGraphiteConfig).queueSize();
        verify(mockFactory).createAsyncMetricObserver(
                metricObserver, QUEUE_SIZE, EXPIRE_TIME);
    }

    @Test
    public void testFactoryCreateAsyncMetricObserver() {
        final MetricObserver metricObserver = realFactory.createAsyncMetricObserver(
                mockMetricObserver, QUEUE_SIZE, EXPIRE_TIME);
        assertEquals(ASYNC_METRIC_OBSERVER_NAME, metricObserver.getName());
        assertEquals(AsyncMetricObserver.class, metricObserver.getClass());
    }

    @Test
    public void testFactoryCreateCounterToRateMetricTransform() {
        when(mockMetricObserver.getName()).thenReturn(ASYNC_METRIC_OBSERVER_NAME);

        final MetricObserver metricObserver = realFactory.createCounterToRateMetricTransform(
                mockMetricObserver, HEARTBEAT, TimeUnit.SECONDS);

        assertEquals(ASYNC_METRIC_OBSERVER_NAME, metricObserver.getName());
        assertEquals(CounterToRateMetricTransform.class, metricObserver.getClass());
        verify(mockMetricObserver).getName();
    }

    @Test
    public void testFactoryCreateGraphiteMetricObserver() {
        final MetricObserver metricObserver = realFactory.createGraphiteMetricObserver(PREFIX, ADDRESS_AND_PORT);
        assertEquals("GraphiteMetricObserver" + PREFIX, metricObserver.getName());
        assertEquals(GraphiteMetricObserver.class, metricObserver.getClass());
    }

    @Test
    public void testDefaultConstructor() {
        new MetricPublishing();
    }
}
