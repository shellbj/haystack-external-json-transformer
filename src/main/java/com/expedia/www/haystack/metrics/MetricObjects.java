package com.expedia.www.haystack.metrics;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.SmallTagMap;
import com.netflix.servo.tag.TaggingContext;
import com.netflix.servo.tag.Tags;

public class MetricObjects {
    static final String TAG_KEY_SUBSYSTEM = "subsystem";
    static final String TAG_KEY_CLASS = "class";
    static Factory factory = new Factory(); // will be mocked out in unit tests

    /**
     * Creates a new Counter; you should only call this method once for each Counter in your code.
     *
     * @param subsystem   the subsystem, typically something like "pipes" or "trends"
     * @param klass       the metric class, frequently (but not necessarily) the class containing the counter the module
     * @param counterName the name of the counter, usually the name of the variable holding the Counter instance
     * @return the Counter that will be able to talk to the InfluxDb; this Counter will be registered in the
     * DefaultMonitorRegistry before it is returned.
     */
    public static Counter createAndRegisterCounter(String subsystem, String klass, String counterName) {
        final TaggingContext taggingContext = () -> {
            final SmallTagMap.Builder builder = new SmallTagMap.Builder(2);
            builder.add(Tags.newTag(TAG_KEY_SUBSYSTEM, subsystem));
            builder.add(Tags.newTag(TAG_KEY_CLASS, klass));
            return new BasicTagList(builder.result());
        };
        final Counter counter = Monitors.newCounter(counterName, taggingContext);
        factory.getMonitorRegistry().register(counter);
        return counter;
    }

    static class Factory {
        MonitorRegistry getMonitorRegistry() {
            return DefaultMonitorRegistry.getInstance();
        }
    }
}

