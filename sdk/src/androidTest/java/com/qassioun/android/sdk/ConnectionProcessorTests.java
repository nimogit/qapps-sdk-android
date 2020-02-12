/*
Copyright (c) Kassioun 2014-"$(date +%Y)" Walid Elhadi - All rights reserved.

*/
package com.qassioun.android.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static com.qassioun.android.sdk.UtilsNetworking.sha1Hash;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ConnectionProcessorTests {
    ConnectionProcessor connectionProcessor;
    QappsStore mockStore;
    DeviceId mockDeviceId;
    String testDeviceId;

    @Before
    public void setUp() {
        mockStore = mock(QappsStore.class);
        mockDeviceId = mock(DeviceId.class);
        connectionProcessor = new ConnectionProcessor("http://server", mockStore, mockDeviceId, null, null);
        testDeviceId = "123";
    }

    @Test
    public void testConstructorAndGetters() {
        final String serverURL = "https://secureserver";
        final QappsStore mockStore = mock(QappsStore.class);
        final DeviceId mockDeviceId = mock(DeviceId.class);
        final ConnectionProcessor connectionProcessor1 = new ConnectionProcessor(serverURL, mockStore, mockDeviceId, null, null);
        assertEquals(serverURL, connectionProcessor1.getServerURL());
        assertSame(mockStore, connectionProcessor1.getQappsStore());
        assertSame(mockDeviceId, connectionProcessor1.getDeviceId());
    }

    @Test
    public void testUrlConnectionForEventData() throws IOException {
        final String eventData = "blahblahblah";
        final URLConnection urlConnection = connectionProcessor.urlConnectionForServerRequest(eventData, null);
        assertEquals(30000, urlConnection.getConnectTimeout());
        assertEquals(30000, urlConnection.getReadTimeout());
        assertFalse(urlConnection.getUseCaches());
        assertTrue(urlConnection.getDoInput());
        assertFalse(urlConnection.getDoOutput());
        assertEquals(new URL(connectionProcessor.getServerURL() + "/i?" + eventData + "&checksum=" + sha1Hash(eventData + null)), urlConnection.getURL());
    }

    @Test
    public void testRun_storeReturnsNullConnections() throws IOException {
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(null);
        connectionProcessor.run();
        verify(mockStore).connections();
        verify(connectionProcessor, times(0)).urlConnectionForServerRequest(anyString(), isNull(String.class));
    }

    @Test
    public void testRun_storeReturnsEmptyConnections() throws IOException {
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(new String[0]);
        connectionProcessor.run();
        verify(mockStore).connections();
        verify(connectionProcessor, times(0)).urlConnectionForServerRequest(anyString(), isNull(String.class));
    }

    private static class TestInputStream extends InputStream {
        int readCount = 0;
        boolean fullyRead() { return readCount >= 2; }
        boolean closed = false;

        @Override
        public int read() {
            return readCount++ < 1 ? 1 : -1;
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    private static class QappsResponseStream extends ByteArrayInputStream {
        boolean closed = false;

        QappsResponseStream(final String result) throws UnsupportedEncodingException {
            super(("{\"result\":\"" + result + "\"}").getBytes("UTF-8"));
        }

        boolean fullyRead() { return pos == buf.length; }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    @Test
    public void testRun_storeHasSingleConnection() throws IOException {
        final String eventData = "blahblahblah";
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(new String[]{eventData}, new String[0]);
        when(mockDeviceId.getId()).thenReturn(testDeviceId);
        final HttpURLConnection mockURLConnection = mock(HttpURLConnection.class);
        final QappsResponseStream testInputStream = new QappsResponseStream("Success");
        when(mockURLConnection.getInputStream()).thenReturn(testInputStream);
        when(mockURLConnection.getResponseCode()).thenReturn(200);
        doReturn(mockURLConnection).when(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        connectionProcessor.run();

        verify(mockStore, times(2)).connections();
        verify(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        verify(mockURLConnection).connect();
        verify(mockURLConnection).getInputStream();
        verify(mockURLConnection).getResponseCode();
        assertTrue(testInputStream.fullyRead());
        verify(mockStore).removeConnection(eventData);
        assertTrue(testInputStream.closed);
        verify(mockURLConnection).disconnect();
    }

    @Test
    public void testRun_storeHasSingleConnection_butHTTPResponseCodeWasNot2xx() throws IOException {
        final String eventData = "blahblahblah";
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(new String[]{eventData}, new String[0]);
        when(mockDeviceId.getId()).thenReturn(testDeviceId);
        final HttpURLConnection mockURLConnection = mock(HttpURLConnection.class);
        final QappsResponseStream testInputStream = new QappsResponseStream("Success");
        when(mockURLConnection.getInputStream()).thenReturn(testInputStream);
        when(mockURLConnection.getResponseCode()).thenReturn(300);
        doReturn(mockURLConnection).when(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        connectionProcessor.run();

        verify(mockStore, times(2)).connections();
        verify(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        verify(mockURLConnection).connect();
        verify(mockURLConnection).getInputStream();
        verify(mockURLConnection).getResponseCode();
        assertTrue(testInputStream.fullyRead());
        verify(mockStore, times(0)).removeConnection(eventData);
        assertTrue(testInputStream.closed);
        verify(mockURLConnection).disconnect();
    }

    @Test
    public void testRun_storeHasSingleConnection_butResponseWasNotJSON() throws IOException {
        final String eventData = "blahblahblah";
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(new String[]{eventData}, new String[0]);
        when(mockDeviceId.getId()).thenReturn(testDeviceId);
        final HttpURLConnection mockURLConnection = mock(HttpURLConnection.class);
        final TestInputStream testInputStream = new TestInputStream();
        when(mockURLConnection.getInputStream()).thenReturn(testInputStream);
        when(mockURLConnection.getResponseCode()).thenReturn(200);
        doReturn(mockURLConnection).when(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        connectionProcessor.run();

        verify(mockStore, times(2)).connections();
        verify(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        verify(mockURLConnection).connect();
        verify(mockURLConnection).getInputStream();
        verify(mockURLConnection).getResponseCode();
        assertTrue(testInputStream.fullyRead());
        verify(mockStore, times(0)).removeConnection(eventData);
        assertTrue(testInputStream.closed);
        verify(mockURLConnection).disconnect();
    }

    @Test
    public void testRun_storeHasSingleConnection_butResponseJSONWasNotSuccess() throws IOException {
        final String eventData = "blahblahblah";
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(new String[]{eventData}, new String[0]);
        when(mockDeviceId.getId()).thenReturn(testDeviceId);
        final HttpURLConnection mockURLConnection = mock(HttpURLConnection.class);
        final QappsResponseStream testInputStream = new QappsResponseStream("Failed");
        when(mockURLConnection.getInputStream()).thenReturn(testInputStream);
        when(mockURLConnection.getResponseCode()).thenReturn(200);
        doReturn(mockURLConnection).when(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        connectionProcessor.run();
        verify(mockStore, times(2)).connections();
        verify(connectionProcessor).urlConnectionForServerRequest(eventData + "&device_id=" + testDeviceId, null);
        verify(mockURLConnection).connect();
        verify(mockURLConnection).getInputStream();
        assertTrue(testInputStream.fullyRead());
        verify(mockURLConnection).getResponseCode();
        verify(mockStore, times(0)).removeConnection(eventData);
        assertTrue(testInputStream.closed);
        verify(mockURLConnection).disconnect();
    }

    @Test
    public void testRun_storeHasTwoConnections() throws IOException {
        final String eventData1 = "blahblahblah";
        final String eventData2 = "123523523432";
        connectionProcessor = spy(connectionProcessor);
        when(mockStore.connections()).thenReturn(new String[]{eventData1, eventData2}, new String[]{eventData2}, new String[0]);
        when(mockDeviceId.getId()).thenReturn(testDeviceId);
        final HttpURLConnection mockURLConnection = mock(HttpURLConnection.class);
        final QappsResponseStream testInputStream1 = new QappsResponseStream("Success");
        final QappsResponseStream testInputStream2 = new QappsResponseStream("Success");
        when(mockURLConnection.getInputStream()).thenReturn(testInputStream1, testInputStream2);
        doReturn(mockURLConnection).when(connectionProcessor).urlConnectionForServerRequest(eventData1 + "&device_id=" + testDeviceId, null);
        doReturn(mockURLConnection).when(connectionProcessor).urlConnectionForServerRequest(eventData2 + "&device_id=" + testDeviceId, null);
        when(mockURLConnection.getResponseCode()).thenReturn(200, 200);
        connectionProcessor.run();
        verify(mockStore, times(3)).connections();
        verify(connectionProcessor).urlConnectionForServerRequest(eventData1 + "&device_id=" + testDeviceId, null);
        verify(connectionProcessor).urlConnectionForServerRequest(eventData2 + "&device_id=" + testDeviceId, null);
        verify(mockURLConnection, times(2)).connect();
        verify(mockURLConnection, times(2)).getInputStream();
        verify(mockURLConnection, times(2)).getResponseCode();
        assertTrue(testInputStream1.fullyRead());
        assertTrue(testInputStream2.fullyRead());
        verify(mockStore).removeConnection(eventData1);
        verify(mockStore).removeConnection(eventData2);
        assertTrue(testInputStream1.closed);
        assertTrue(testInputStream2.closed);
        verify(mockURLConnection, times(2)).disconnect();
    }

    private static class TestInputStream2 extends InputStream {
        boolean closed = false;

        @Override
        public int read() throws IOException {
            throw new IOException();
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }
}
