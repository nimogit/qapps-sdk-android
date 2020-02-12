/*
Copyright (c) Kassioun 2014-"$(date +%Y)" Walid Elhadi - All rights reserved.

*/
package com.qassioun.android.sdk;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;

import java.util.HashMap;

import static androidx.test.InstrumentationRegistry.getContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

@RunWith(AndroidJUnit4.class)
public class QappsTests {
    Qapps mUninitedQapps;
    Qapps mQapps;

    @Before
    public void setUp() {
        final QappsStore qappsStore = new QappsStore(getContext());
        qappsStore.clear();

        mUninitedQapps = new Qapps();

        mQapps = new Qapps();
        mQapps.init((new QappsConfig(getContext(), "appkey", "http://test.qassioun.com")).setDeviceId("1234").setLoggingEnabled(true));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        assertNotNull(mUninitedQapps.getConnectionQueue());
        assertNull(mUninitedQapps.getConnectionQueue().getContext());
        assertNull(mUninitedQapps.getConnectionQueue().getServerURL());
        assertNull(mUninitedQapps.getConnectionQueue().getAppKey());
        assertNull(mUninitedQapps.getConnectionQueue().getQappsStore());
        assertNotNull(mUninitedQapps.getTimerService());
        assertNull(mUninitedQapps.getEventQueue());
        assertEquals(0, mUninitedQapps.getActivityCount());
        assertEquals(0, mUninitedQapps.getPrevSessionDurationStartTime());
        assertFalse(mUninitedQapps.getDisableUpdateSessionRequests());
        assertFalse(mUninitedQapps.isLoggingEnabled());
    }

    @Test
    public void testSharedInstance() {
        Qapps sharedQapps = Qapps.sharedInstance();
        assertNotNull(sharedQapps);
        assertSame(sharedQapps, Qapps.sharedInstance());
    }

    @Test
    public void testInitWithNoDeviceID() {
        mUninitedQapps = spy(mUninitedQapps);
        QappsConfig cc = (new QappsConfig(getContext(), "appkey", "http://test.qassioun.com"));
        mUninitedQapps.init(cc);
        verify(mUninitedQapps).init(cc);
    }

    @Test
    public void testInit_nullContext() {
        try {
            mUninitedQapps.init(null, "http://test.qassioun.com", "appkey", "1234");
            fail("expected null context to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_nullServerURL() {
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey", null)).setDeviceId("1234"));
            fail("expected null server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_emptyServerURL() {
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "")).setDeviceId("1234"));
            fail("expected empty server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_invalidServerURL() {
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "not-a-valid-server-url")).setDeviceId("1234"));
            fail("expected invalid server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_nullAppKey() {
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), null, "http://test.qassioun.com")).setDeviceId("1234"));
            fail("expected null app key to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_emptyAppKey() {
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "", "http://test.qassioun.com")).setDeviceId("1234"));
            fail("expected empty app key to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_nullDeviceID() {
        // null device ID is okay because it tells Qapps to use OpenUDID
       mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "http://test.qassioun.com")).setDeviceId(null));
    }

    @Test
    public void testInit_emptyDeviceID() {
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "http://test.qassioun.com")).setDeviceId(""));
            fail("expected empty device ID to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_twiceWithSameParams() {
        final String deviceID = "1234";
        final String appKey = "appkey";
        final String serverURL = "http://test.qassioun.com";

        mUninitedQapps.init((new QappsConfig(getContext(), appKey, serverURL)).setDeviceId(deviceID));
        final EventQueue expectedEventQueue = mUninitedQapps.getEventQueue();
        final ConnectionQueue expectedConnectionQueue = mUninitedQapps.getConnectionQueue();
        final QappsStore expectedQappsStore = expectedConnectionQueue.getQappsStore();
        assertNotNull(expectedEventQueue);
        assertNotNull(expectedConnectionQueue);
        assertNotNull(expectedQappsStore);

        // second call with same params should succeed, no exception thrown
        mUninitedQapps.init((new QappsConfig(getContext(), appKey, serverURL)).setDeviceId(deviceID));

        assertSame(expectedEventQueue, mUninitedQapps.getEventQueue());
        assertSame(expectedConnectionQueue, mUninitedQapps.getConnectionQueue());
        assertSame(expectedQappsStore, mUninitedQapps.getConnectionQueue().getQappsStore());
        assertSame(getContext().getApplicationContext(), mUninitedQapps.getConnectionQueue().getContext());
        assertEquals(serverURL, mUninitedQapps.getConnectionQueue().getServerURL());
        assertEquals(appKey, mUninitedQapps.getConnectionQueue().getAppKey());
        assertSame(mUninitedQapps.getConnectionQueue().getQappsStore(), mUninitedQapps.getEventQueue().getQappsStore());
    }

    @Test
    public void testInit_twiceWithDifferentContext() {
        mUninitedQapps.init(getContext(), "http://test.qassioun.com", "appkey", "1234");
        // changing context is okay since SharedPrefs are global singletons

        Context mContext = mock(Context.class);
        when(mContext.getCacheDir()).thenReturn(getContext().getCacheDir());

        mUninitedQapps.init(mContext, "http://test.qassioun.com", "appkey", "1234");
    }

    @Test
    public void testInit_twiceWithDifferentServerURL() {
        mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "http://test1.qassioun.com")).setDeviceId("1234"));
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "http://test2.qassioun.com")).setDeviceId("1234"));
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_twiceWithDifferentAppKey() {
        mUninitedQapps.init((new QappsConfig(getContext(), "appkey1", "http://test.qassioun.com")).setDeviceId("1234"));
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey2", "http://test.qassioun.com")).setDeviceId("1234"));
            fail("expected IllegalStateException to be thrown when calling init a second time with different app key");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_twiceWithDifferentDeviceID() {
        mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "http://test.qassioun.com")).setDeviceId("1234"));
        try {
            mUninitedQapps.init((new QappsConfig(getContext(), "appkey", "http://test.qassioun.com")).setDeviceId("4321"));
            fail("expected IllegalStateException to be thrown when calling init a second time with different device ID");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testInit_normal() {
        final String deviceID = "1234";
        final String appKey = "appkey";
        final String serverURL = "http://test.qassioun.com";

        mUninitedQapps.init((new QappsConfig(getContext(), appKey, serverURL)).setDeviceId(deviceID));

        assertSame(getContext().getApplicationContext(), mUninitedQapps.getConnectionQueue().getContext());
        assertEquals(serverURL, mUninitedQapps.getConnectionQueue().getServerURL());
        assertEquals(appKey, mUninitedQapps.getConnectionQueue().getAppKey());
        assertNotNull(mUninitedQapps.getConnectionQueue().getQappsStore());
        assertNotNull(mUninitedQapps.getEventQueue());
        assertSame(mUninitedQapps.getConnectionQueue().getQappsStore(), mUninitedQapps.getEventQueue().getQappsStore());
    }

    @Test
    public void testHalt_notInitialized() {
        mUninitedQapps.halt();
        assertNotNull(mUninitedQapps.getConnectionQueue());
        assertNull(mUninitedQapps.getConnectionQueue().getContext());
        assertNull(mUninitedQapps.getConnectionQueue().getServerURL());
        assertNull(mUninitedQapps.getConnectionQueue().getAppKey());
        assertNull(mUninitedQapps.getConnectionQueue().getQappsStore());
        assertNotNull(mUninitedQapps.getTimerService());
        assertNull(mUninitedQapps.getEventQueue());
        assertEquals(0, mUninitedQapps.getActivityCount());
        assertEquals(0, mUninitedQapps.getPrevSessionDurationStartTime());
    }

    @Test
    public void testHalt() {
        QappsStore mockQappsStore = mock(QappsStore.class);

        when(mockQappsStore.getLocationDisabled()).thenReturn(true);
        when(mockQappsStore.getCachedAdvertisingId()).thenReturn("");

        mQapps.getConnectionQueue().setQappsStore(mockQappsStore);
        mQapps.onStart(null);
        assertTrue(0 != mQapps.getPrevSessionDurationStartTime());
        assertTrue(0 != mQapps.getActivityCount());
        assertNotNull(mQapps.getEventQueue());
        assertNotNull(mQapps.getConnectionQueue().getContext());
        assertNotNull(mQapps.getConnectionQueue().getServerURL());
        assertNotNull(mQapps.getConnectionQueue().getAppKey());
        assertNotNull(mQapps.getConnectionQueue().getContext());

        mQapps.halt();

        verify(mockQappsStore).clear();
        assertNotNull(mQapps.getConnectionQueue());
        assertNull(mQapps.getConnectionQueue().getContext());
        assertNull(mQapps.getConnectionQueue().getServerURL());
        assertNull(mQapps.getConnectionQueue().getAppKey());
        assertNull(mQapps.getConnectionQueue().getQappsStore());
        assertNotNull(mQapps.getTimerService());
        assertNull(mQapps.getEventQueue());
        assertEquals(0, mQapps.getActivityCount());
        assertEquals(0, mQapps.getPrevSessionDurationStartTime());
    }

    @Test
    public void testOnStart_initNotCalled() {
        try {
            mUninitedQapps.onStart(null);
            fail("expected calling onStart before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testOnStart_firstCall() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        mQapps.onStart(null);

        assertEquals(1, mQapps.getActivityCount());
        final long prevSessionDurationStartTime = mQapps.getPrevSessionDurationStartTime();
        assertTrue(prevSessionDurationStartTime > 0);
        assertTrue(prevSessionDurationStartTime <= System.nanoTime());
        verify(mockConnectionQueue).beginSession();
    }

    @Test
    public void testOnStart_subsequentCall() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        mQapps.onStart(null); // first call to onStart
        final long prevSessionDurationStartTime = mQapps.getPrevSessionDurationStartTime();
        mQapps.onStart(null); // second call to onStart

        assertEquals(2, mQapps.getActivityCount());
        assertEquals(prevSessionDurationStartTime, mQapps.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue).beginSession();
    }

    @Test
    public void testOnStop_initNotCalled() {
        try {
            mUninitedQapps.onStop();
            fail("expected calling onStop before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testOnStop_unbalanced() {
        try {
            mQapps.onStop();
            fail("expected calling onStop before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testOnStop_reallyStopping_emptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        mQapps.onStart(null);
        mQapps.onStop();

        assertEquals(0, mQapps.getActivityCount());
        assertEquals(0, mQapps.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue).endSession(0);
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    @Test
    public void testOnStop_reallyStopping_nonEmptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        mQapps.setEventQueue(mockEventQueue);

        when(mockEventQueue.size()).thenReturn(1);
        final String eventStr = "blahblahblahblah";
        when(mockEventQueue.events()).thenReturn(eventStr);

        mQapps.onStart(null);
        mQapps.onStop();

        assertEquals(0, mQapps.getActivityCount());
        assertEquals(0, mQapps.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue).endSession(0);
        verify(mockConnectionQueue).recordEvents(eventStr);
    }

    @Test
    public void testOnStop_notStopping() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        mQapps.onStart(null);
        mQapps.onStart(null);
        final long prevSessionDurationStartTime = mQapps.getPrevSessionDurationStartTime();
        mQapps.onStop();

        assertEquals(1, mQapps.getActivityCount());
        assertEquals(prevSessionDurationStartTime, mQapps.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue, times(0)).endSession(anyInt());
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    @Test
    public void testRecordEvent_keyOnly() {
        final String eventKey = "eventKey";
        final Qapps qapps = spy(mQapps);
        doNothing().when(qapps).recordEvent(eventKey, null, 1, 0.0d);
        qapps.recordEvent(eventKey);
        verify(qapps).recordEvent(eventKey, null, 1, 0.0d);
    }

    @Test
    public void testRecordEvent_keyAndCount() {
        final String eventKey = "eventKey";
        final int count = 42;
        final Qapps qapps = spy(mQapps);

        doNothing().when(qapps).recordEvent(eventKey, null, count, 0.0d);
        qapps.recordEvent(eventKey, null, count, 0.0d);
        verify(qapps).recordEvent(eventKey, null, count, 0.0d);
    }

    @Test
    public void testRecordEvent_keyAndCountAndSum() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final Qapps qapps = spy(mQapps);
        doNothing().when(qapps).recordEvent(eventKey, null, count, sum);
        qapps.recordEvent(eventKey, count, sum);
        verify(qapps).recordEvent(eventKey, null, count, sum);
    }

    @Test
    public void testRecordEvent_keyAndSegmentationAndCount() {
        final String eventKey = "eventKey";
        final int count = 42;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");
        final Qapps qapps = spy(mQapps);
        doNothing().when(qapps).recordEvent(eventKey, segmentation, count, 0.0d);
        qapps.recordEvent(eventKey, segmentation, count);
        verify(qapps).recordEvent(eventKey, segmentation, count, 0.0d);
    }

    @Test
    public void testRecordEvent_initNotCalled() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mUninitedQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalStateException when recordEvent called before init");
        } catch (IllegalStateException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_nullKey() {
        final String eventKey = null;
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            //noinspection ConstantConditions
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with null key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_emptyKey() {
        final String eventKey = "";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with empty key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_countIsZero() {
        final String eventKey = "";
        final int count = 0;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with count=0");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_countIsNegative() {
        final String eventKey = "";
        final int count = -1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with a negative count");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_segmentationHasNullKey() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put(null, "segvalue1");

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with null key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_segmentationHasEmptyKey() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("", "segvalue1");

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with empty key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_segmentationHasNullValue() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", null);

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with null value");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent_segmentationHasEmptyValue() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "");

        try {
            mQapps.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with empty value");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    @Test
    public void testRecordEvent() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final double dur = 10.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");
        final HashMap<String, Double> segmD = new HashMap<>();
        final HashMap<String, Integer> segmI = new HashMap<>();

        final EventQueue mockEventQueue = mock(EventQueue.class);
        mQapps.setEventQueue(mockEventQueue);

        final Qapps qapps = spy(mQapps);
        doNothing().when(qapps).sendEventsIfNeeded();
        qapps.recordEvent(eventKey, segmentation, count, sum, dur);

        verify(mockEventQueue).recordEvent(eventKey, segmentation, segmI, segmD, count, sum, dur, null);
        verify(qapps).sendEventsIfNeeded();
    }

    @Test
    public void testSendEventsIfNeeded_emptyQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(0);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.sendEventsIfNeeded();

        verify(mockEventQueue, times(0)).events();
        verifyZeroInteractions(mockConnectionQueue);
    }

    @Test
    public void testSendEventsIfNeeded_lessThanThreshold() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(9);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.sendEventsIfNeeded();

        verify(mockEventQueue, times(0)).events();
        verifyZeroInteractions(mockConnectionQueue);
    }

    @Test
    public void testSendEventsIfNeeded_equalToThreshold() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(10);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.sendEventsIfNeeded();

        verify(mockEventQueue, times(1)).events();
        verify(mockConnectionQueue, times(1)).recordEvents(eventData);
    }

    @Test
    public void testSendEventsIfNeeded_moreThanThreshold() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(20);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.sendEventsIfNeeded();

        verify(mockEventQueue, times(1)).events();
        verify(mockConnectionQueue, times(1)).recordEvents(eventData);
    }

    @Test
    public void testOnTimer_noActiveSession() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.onTimer();

        verifyZeroInteractions(mockEventQueue);
        verify(mockConnectionQueue).tick();
    }

    @Test
    public void testOnTimer_activeSession_emptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(0);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.onStart(null);
        mQapps.onTimer();

        verify(mockConnectionQueue).updateSession(0);
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    @Test
    public void testOnTimer_activeSession_nonEmptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(1);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.onStart(null);
        mQapps.onTimer();

        verify(mockConnectionQueue).updateSession(0);
        verify(mockConnectionQueue).recordEvents(eventData);
    }

    @Test
    public void testOnTimer_activeSession_emptyEventQueue_sessionTimeUpdatesDisabled() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);
        mQapps.setDisableUpdateSessionRequests(true);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(0);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.onStart(null);
        mQapps.onTimer();

        verify(mockConnectionQueue, times(0)).updateSession(anyInt());
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    @Test
    public void testOnTimer_activeSession_nonEmptyEventQueue_sessionTimeUpdatesDisabled() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mQapps.setConnectionQueue(mockConnectionQueue);
        mQapps.setDisableUpdateSessionRequests(true);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(1);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mQapps.setEventQueue(mockEventQueue);

        mQapps.onStart(null);
        mQapps.onTimer();

        verify(mockConnectionQueue, times(0)).updateSession(anyInt());
        verify(mockConnectionQueue).recordEvents(eventData);
    }

    @Test
    public void testRoundedSecondsSinceLastSessionDurationUpdate() {
        long prevSessionDurationStartTime = System.nanoTime() - 1000000000;
        mQapps.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(1, mQapps.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 2000000000;
        mQapps.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(2, mQapps.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 1600000000;
        mQapps.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(2, mQapps.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 1200000000;
        mQapps.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(1, mQapps.roundedSecondsSinceLastSessionDurationUpdate());
    }

    @Test
    public void testIsValidURL_badURLs() {
        assertFalse(UtilsNetworking.isValidURL(null));
        assertFalse(UtilsNetworking.isValidURL(""));
        assertFalse(UtilsNetworking.isValidURL(" "));
        assertFalse(UtilsNetworking.isValidURL("blahblahblah.com"));
    }

    @Test
    public void testIsValidURL_goodURL() {
        assertTrue(UtilsNetworking.isValidURL("http://test.qassioun.com"));
    }

    @Test
    public void testCurrentTimestamp() {
        final int testTimestamp = (int) (System.currentTimeMillis() / 1000L);
        final int actualTimestamp = UtilsTime.currentTimestampSeconds();
        assertTrue(((testTimestamp - 1) <= actualTimestamp) && ((testTimestamp + 1) >= actualTimestamp));
    }

    @Test
    public void testSetDisableUpdateSessionRequests() {
        assertFalse(mQapps.getDisableUpdateSessionRequests());
        mQapps.setDisableUpdateSessionRequests(true);
        assertTrue(mQapps.getDisableUpdateSessionRequests());
        mQapps.setDisableUpdateSessionRequests(false);
        assertFalse(mQapps.getDisableUpdateSessionRequests());
    }

    @Test
    public void testLoggingFlag() {
        assertFalse(mUninitedQapps.isLoggingEnabled());
        mUninitedQapps.setLoggingEnabled(true);
        assertTrue(mUninitedQapps.isLoggingEnabled());
        mUninitedQapps.setLoggingEnabled(false);
        assertFalse(mUninitedQapps.isLoggingEnabled());
    }
}
