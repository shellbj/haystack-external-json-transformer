package com.expedia.www.haystack.metrics;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.tag.TagList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_CLASS;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_SUBSYSTEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricObjectsTest {
    private final static Random RANDOM = new Random();
    private final static String SUBSYSTEM = RANDOM.nextLong() + "SUBSYSTEM";
    private final static String CLASS = RANDOM.nextLong() + "CLASS";
    private final static String COUNTER_NAME = RANDOM.nextLong() + "COUNTER_NAME";

    @Mock
    private MetricObjects.Factory mockFactory;
    private MetricObjects.Factory realFactory;

    @Mock
    private MonitorRegistry mockMonitorRegistry;

    @Before
    public void setUp() {
        realFactory = MetricObjects.factory;
        MetricObjects.factory = mockFactory;
    }

    @After
    public void tearDown() {
        MetricObjects.factory = realFactory;
        verifyNoMoreInteractions(mockFactory, mockMonitorRegistry);
    }

    @Test
    public void testCreateAndRegisterCounter() {
        when(mockFactory.getMonitorRegistry()).thenReturn(mockMonitorRegistry);

        final Counter counter = MetricObjects.createAndRegisterCounter(SUBSYSTEM, CLASS, COUNTER_NAME);

        final TagList tags = counter.getConfig().getTags();
        assertEquals(2, tags.size());
        assertEquals(SUBSYSTEM, tags.getValue(TAG_KEY_SUBSYSTEM));
        assertEquals(CLASS, tags.getValue(TAG_KEY_CLASS));
        assertEquals(COUNTER_NAME, counter.getConfig().getName());
        verify(mockMonitorRegistry).register(counter);
        verify(mockFactory).getMonitorRegistry();
    }

    @Test
    public void testFactoryGetDefaultMonitorRegisterInstance() {
        assertSame(DefaultMonitorRegistry.getInstance(), realFactory.getMonitorRegistry());
    }

    @Test
    public void testDefaultConstructor() {
        new MetricObjects();
    }
}
