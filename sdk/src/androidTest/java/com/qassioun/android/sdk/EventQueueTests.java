/*
Copyright (c) Kassioun 2014-"$(date +%Y)" Walid Elhadi - All rights reserved.

*/
package com.qassioun.android.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventQueueTests {
    EventQueue mEventQueue;
    QappsStore mMockQappsStore;

    @Before
    public void setUp() {

        mMockQappsStore = mock(QappsStore.class);
        mEventQueue = new EventQueue(mMockQappsStore);
    }

    @Test
    public void testConstructor() {
        assertSame(mMockQappsStore, mEventQueue.getQappsStore());
    }

    @Test
    public void testRecordEvent() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final double dur = 10.0d;
        final Map<String, String> segmentation = new HashMap<>(1);
        final Map<String, Integer> segmentationInt = new HashMap<>(2);
        final Map<String, Double> segmentationDouble = new HashMap<>(3);
        UtilsTime.Instant instant = UtilsTime.getCurrentInstant();
        final long timestamp = instant.timestampMs;
        final int hour = instant.hour;
        final int dow = instant.dow;
        final ArgumentCaptor<Long> arg = ArgumentCaptor.forClass(Long.class);

        mEventQueue.recordEvent(eventKey, segmentation, segmentationInt, segmentationDouble, count, sum, dur, null);
        verify(mMockQappsStore).addEvent(eq(eventKey), eq(segmentation), eq(segmentationInt), eq(segmentationDouble), arg.capture(), eq(hour), eq(dow), eq(count), eq(sum), eq(dur));
        assertTrue(((timestamp - 1) <= arg.getValue()) && ((timestamp + 1) >= arg.getValue()));
    }

    @Test
    public void testRecordPastEvent() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final double dur = 10.0d;
        final Map<String, String> segmentation = new HashMap<>(1);
        final Map<String, Integer> segmentationInt = new HashMap<>(2);
        final Map<String, Double> segmentationDouble = new HashMap<>(3);
        UtilsTime.Instant instant = UtilsTime.Instant.get(123456789);
        final long timestamp = instant.timestampMs;
        final int hour = instant.hour;
        final int dow = instant.dow;
        final ArgumentCaptor<Long> arg = ArgumentCaptor.forClass(Long.class);

        mEventQueue.recordEvent(eventKey, segmentation, segmentationInt, segmentationDouble, count, sum, dur, instant);
        verify(mMockQappsStore).addEvent(eq(eventKey), eq(segmentation), eq(segmentationInt), eq(segmentationDouble), arg.capture(), eq(hour), eq(dow), eq(count), eq(sum), eq(dur));
        assertTrue(((timestamp - 1) <= arg.getValue()) && ((timestamp + 1) >= arg.getValue()));
    }

    @Test
    public void testSize_zeroLenArray() {
        when(mMockQappsStore.events()).thenReturn(new String[0]);
        assertEquals(0, mEventQueue.size());
    }

    @Test
    public void testSize() {
        when(mMockQappsStore.events()).thenReturn(new String[2]);
        assertEquals(2, mEventQueue.size());
    }

    @Test
    public void testEvents_emptyList() throws UnsupportedEncodingException {
        final List<Event> eventsList = new ArrayList<>();
        when(mMockQappsStore.eventsList()).thenReturn(eventsList);

        final String expected = URLEncoder.encode("[]", "UTF-8");
        assertEquals(expected, mEventQueue.events());
        verify(mMockQappsStore).eventsList();
        verify(mMockQappsStore).removeEvents(eventsList);
    }

    @Test
    public void testEvents_nonEmptyList() throws UnsupportedEncodingException {
        final List<Event> eventsList = new ArrayList<>();
        final Event event1 = new Event();
        event1.key = "event1Key";
        eventsList.add(event1);
        final Event event2 = new Event();
        event2.key = "event2Key";
        eventsList.add(event2);
        when(mMockQappsStore.eventsList()).thenReturn(eventsList);

        final String jsonToEncode = "[" + event1.toJSON().toString() + "," + event2.toJSON().toString() + "]";
        final String expected = URLEncoder.encode(jsonToEncode, "UTF-8");
        assertEquals(expected, mEventQueue.events());
        verify(mMockQappsStore).eventsList();
        verify(mMockQappsStore).removeEvents(eventsList);
    }
}
