/*
Copyright (c) Kassioun 2014-"$(date +%Y)" Walid Elhadi - All rights reserved.

*/
package com.qassioun.android.sdk;

import android.app.Instrumentation;
import android.net.Uri;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static androidx.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ConnectionQueueTests {
    ConnectionQueue connQ;
    ConnectionQueue freshConnQ;
    final static long timestampAllowance = 150;

    @Before
    public void setUp() {
        freshConnQ = new ConnectionQueue();
        connQ = new ConnectionQueue();
        connQ.setAppKey("abcDeFgHiJkLmNoPQRstuVWxyz");
        connQ.setServerURL("http://qapps.coupons.com");
        connQ.setContext(getContext());
        QappsStore cs = mock(QappsStore.class);
        when(cs.getCachedAdvertisingId()).thenReturn("");
        connQ.setQappsStore(cs);
        connQ.setDeviceId(mock(DeviceId.class));
        connQ.setExecutor(mock(ExecutorService.class));
    }

    @Test
    public void testConstructor() {
        assertNull(freshConnQ.getQappsStore());
        assertNull(freshConnQ.getDeviceId());
        assertNull(freshConnQ.getAppKey());
        assertNull(freshConnQ.getContext());
        assertNull(freshConnQ.getServerURL());
        assertNull(freshConnQ.getExecutor());
    }

    @Test
    public void testAppKey() {
        final String appKey = "blahblahblah";
        freshConnQ.setAppKey(appKey);
        assertEquals(appKey, freshConnQ.getAppKey());
    }

    @Test
    public void testContext() {
        freshConnQ.setContext(getContext());
        assertSame(getContext(), freshConnQ.getContext());
    }

    @Test
    public void testServerURL() {
        final String serverURL = "http://qapps.coupons.com";
        freshConnQ.setServerURL(serverURL);
        assertEquals(serverURL, freshConnQ.getServerURL());
    }

    @Test
    public void testQappsStore() {
        final QappsStore store = new QappsStore(getContext());
        freshConnQ.setQappsStore(store);
        assertSame(store, freshConnQ.getQappsStore());
    }

    @Test
    public void testDeviceId() {
        final QappsStore store = new QappsStore(getContext());
        final DeviceId deviceId = new DeviceId(store, "blah");
        freshConnQ.setDeviceId(deviceId);
        assertSame(deviceId, freshConnQ.getDeviceId());
    }

    @Test
    public void testExecutor() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        freshConnQ.setExecutor(executor);
        assertSame(executor, freshConnQ.getExecutor());
    }

    @Test
    public void testCheckInternalState_nullAppKey() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setAppKey(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testCheckInternalState_emptyAppKey() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setAppKey("");
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testCheckInternalState_nullStore() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setQappsStore(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testCheckInternalState_nullContext() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setContext(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testCheckInternalState_nullServerURL() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setServerURL(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testCheckInternalState_invalidServerURL() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setServerURL("blahblahblah.com");
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testBeginSession_checkInternalState() {
        try {
            freshConnQ.beginSession();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testBeginSession() throws JSONException, UnsupportedEncodingException {
        connQ.beginSession();
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getQappsStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = UtilsTime.currentTimestampMs();
        final long actualTimestamp = Long.parseLong(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this test
        assertTrue(((curTimestamp-400) <= actualTimestamp) && ((curTimestamp+400) >= actualTimestamp));
        assertEquals(Qapps.QAPPS_SDK_VERSION_STRING, queryParams.get("sdk_version"));
        assertEquals("1", queryParams.get("begin_session"));
        // validate metrics
        final JSONObject actualMetrics = new JSONObject(queryParams.get("metrics"));
        final String metricsJsonStr = URLDecoder.decode(DeviceInfo.getMetrics(getContext()), "UTF-8");
        final JSONObject expectedMetrics = new JSONObject(metricsJsonStr);
        assertEquals(expectedMetrics.length(), actualMetrics.length());
        final Iterator actualMetricsKeyIterator = actualMetrics.keys();
        while (actualMetricsKeyIterator.hasNext()) {
            final String key = (String) actualMetricsKeyIterator.next();
            assertEquals(expectedMetrics.get(key), actualMetrics.get(key));
        }
    }

    @Test
    public void testUpdateSession_checkInternalState() {
        try {
            freshConnQ.updateSession(15);
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testUpdateSession_zeroDuration() {
        connQ.updateSession(0);
        verifyZeroInteractions(connQ.getExecutor(), connQ.getQappsStore());
    }

    @Test
    public void testUpdateSession_negativeDuration() {
        connQ.updateSession(-1);
        verifyZeroInteractions(connQ.getExecutor(), connQ.getQappsStore());
    }

    @Test
    public void testUpdateSession_moreThanZeroDuration() {
        connQ.updateSession(60);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getQappsStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = UtilsTime.currentTimestampMs();
        final long actualTimestamp = Long.parseLong(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this test
        assertTrue(((curTimestamp-400) <= actualTimestamp) && ((curTimestamp+400) >= actualTimestamp));
        assertEquals("60", queryParams.get("session_duration"));
    }

    @Test
    public void testEndSession_checkInternalState() {
        try {
            freshConnQ.endSession(15);
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testEndSession_zeroDuration() {
        connQ.endSession(0);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getQappsStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = UtilsTime.currentTimestampMs();
        final long curTimestampBelow = curTimestamp - timestampAllowance;
        final long curTimestampAbove = curTimestamp + timestampAllowance;
        final long actualTimestamp = Long.parseLong(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this test
        assertTrue((curTimestampBelow <= actualTimestamp) && (curTimestampAbove >= actualTimestamp));
        assertFalse(queryParams.containsKey("session_duration"));
        assertEquals("1", queryParams.get("end_session"));
    }

    @Test
    public void testEndSession_negativeDuration() {
        connQ.endSession(-1);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getQappsStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = UtilsTime.currentTimestampMs();
        final long curTimestampBelow = curTimestamp - timestampAllowance;
        final long curTimestampAbove = curTimestamp + timestampAllowance;
        final long actualTimestamp = Long.parseLong(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this test
        assertTrue((curTimestampBelow <= actualTimestamp) && (curTimestampAbove >= actualTimestamp));
        assertFalse(queryParams.containsKey("session_duration"));
        assertEquals("1", queryParams.get("end_session"));
    }

    @Test
    public void testEndSession_moreThanZeroDuration() {
        connQ.endSession(15);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getQappsStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = UtilsTime.currentTimestampMs();
        final long curTimestampBelow = curTimestamp - timestampAllowance;
        final long curTimestampAbove = curTimestamp + timestampAllowance;
        final long actualTimestamp = Long.parseLong(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this test
        assertTrue((curTimestampBelow <= actualTimestamp) && (curTimestampAbove >= actualTimestamp));
        assertEquals("1", queryParams.get("end_session"));
        assertEquals("15", queryParams.get("session_duration"));
    }

    @Test
    public void testRecordEvents_checkInternalState() {
        try {
            freshConnQ.recordEvents("blahblahblah");
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    @Test
    public void testRecordEvents() {
        final String eventData = "blahblahblah";
        connQ.recordEvents(eventData);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getQappsStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = UtilsTime.currentTimestampMs();
        final long curTimestampBelow = curTimestamp - timestampAllowance;
        final long curTimestampAbove = curTimestamp + timestampAllowance;
        final long actualTimestamp = Long.parseLong(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this test
        assertTrue((curTimestampBelow <= actualTimestamp) && (curTimestampAbove >= actualTimestamp));
        assertEquals(eventData, queryParams.get("events"));
    }

    private Map<String, String> parseQueryParams(final String queryStr) {
        final String urlStr = "http://server?" + queryStr;
        final Uri uri = Uri.parse(urlStr);
        final Set<String> queryParameterNames = uri.getQueryParameterNames();
        final Map<String, String> queryParams = new HashMap<>(queryParameterNames.size());
        for (String paramName : queryParameterNames) {
            queryParams.put(paramName, uri.getQueryParameter(paramName));
        }
        return queryParams;
    }

    @Test
    public void testEnsureExecutor_nullExecutor() {
        assertNull(freshConnQ.getExecutor());
        freshConnQ.ensureExecutor();
        assertNotNull(freshConnQ.getExecutor());
    }

    @Test
    public void testEnsureExecutor_alreadyHasExecutor() {
        ExecutorService executor = connQ.getExecutor();
        assertNotNull(executor);
        connQ.ensureExecutor();
        assertSame(executor, connQ.getExecutor());
    }

    @Test
    public void testTick_storeHasNoConnections() {
        when(connQ.getQappsStore().isEmptyConnections()).thenReturn(true);
        connQ.tick();
        verifyZeroInteractions(connQ.getExecutor());
    }

    @Test
    public void testTick_storeHasConnectionsAndFutureIsNull() {
        final Future mockFuture = mock(Future.class);
        when(connQ.getExecutor().submit(any(ConnectionProcessor.class))).thenReturn(mockFuture);
        connQ.tick();
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));
        assertSame(mockFuture, connQ.getConnectionProcessorFuture());
    }

    @Test
    public void testTick_checkConnectionProcessor() {
        final ArgumentCaptor<Runnable> arg = ArgumentCaptor.forClass(Runnable.class);
        when(connQ.getExecutor().submit(arg.capture())).thenReturn(null);
        connQ.tick();
        assertEquals(((ConnectionProcessor)arg.getValue()).getServerURL(), connQ.getServerURL());
        assertSame(((ConnectionProcessor)arg.getValue()).getQappsStore(), connQ.getQappsStore());
    }

    @Test
    public void testTick_storeHasConnectionsAndFutureIsDone() {
        final Future<?> mockFuture = mock(Future.class);
        when(mockFuture.isDone()).thenReturn(true);
        connQ.setConnectionProcessorFuture(mockFuture);
        final Future mockFuture2 = mock(Future.class);
        when(connQ.getExecutor().submit(any(ConnectionProcessor.class))).thenReturn(mockFuture2);
        connQ.tick();
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));
        assertSame(mockFuture2, connQ.getConnectionProcessorFuture());
    }

    @Test
    public void testTick_storeHasConnectionsButFutureIsNotDone() {
        final Future<?> mockFuture = mock(Future.class);
        connQ.setConnectionProcessorFuture(mockFuture);
        connQ.tick();
        verifyZeroInteractions(connQ.getExecutor());
    }
}
