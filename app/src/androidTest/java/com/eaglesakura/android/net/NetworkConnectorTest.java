package com.eaglesakura.android.net;

import com.eaglesakura.android.net.cache.tkvs.TextCacheController;
import com.eaglesakura.android.net.parser.StringParser;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.net.request.SimpleHttpRequest;
import com.eaglesakura.util.StringUtil;

import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;

public class NetworkConnectorTest extends AndroidTestCase {
    private static final int CONNECT_WAIT_MS = 1000 * 60;

    NetworkConnector newConnector() {
        NetworkConnector connector = NetworkConnector.newDefaultConnector(getContext());
        connector.setCacheController(null);
        return connector;
    }

    public void test_buildURLGet() throws Exception {
        SimpleHttpRequest req = new SimpleHttpRequest(ConnectRequest.Method.GET);


        {
            req.setUrl("https://example.com", null);
            assertEquals(req.getUrl(), "https://example.com");
        }

        {
            Map<String, String> values = new HashMap<>();
            values.put("key", "value");
            req.setUrl("https://example.com", values);
            assertEquals(req.getUrl(), "https://example.com?key=value&");
        }

        {
            Map<String, String> values = new HashMap<>();
            values.put("key", "value");
            req.setUrl("https://example.com?def=value", values);
            assertEquals(req.getUrl(), "https://example.com?def=value&key=value&");
        }
    }

    public void test_buildURLPost() throws Exception {
        SimpleHttpRequest req = new SimpleHttpRequest(ConnectRequest.Method.POST);

        {
            req.setUrl("https://example.com", null);
            assertEquals(req.getUrl(), "https://example.com");
        }

        {
            Map<String, String> values = new HashMap<>();
            values.put("key", "value");
            req.setUrl("https://example.com", values);
            assertEquals(req.getUrl(), "https://example.com");
        }
    }

    public void test_simpleGet() throws Exception {
        NetworkConnector connector = newConnector();
        SimpleHttpRequest req = new SimpleHttpRequest(ConnectRequest.Method.GET);
        req.setUrl("https://www.google.com", null);

        NetworkResult<String> connect = connector.connect(req, StringParser.getInstance());
        assertNotNull(connect);
        assertNotNull(connect.getConnection());
        assertNotNull(connect.getTaskResult());

        String html = connect.await(CONNECT_WAIT_MS);
        assertFalse(StringUtil.isEmpty(html));
    }

    public void test_simpleGetWithCache() throws Exception {
        NetworkConnector connector = newConnector();
        connector.setCacheController(new TextCacheController(getContext()));

        String oldValue = null;
        String newValue = null;

        for (int i = 0; i < 2; ++i) {
            SimpleHttpRequest req = new SimpleHttpRequest(ConnectRequest.Method.GET);
            Map<String, String> values = new HashMap<>();
            values.put("lat", "35.68653");
            values.put("lon", "139.69193");
            req.setUrl("http://geocode.didit.jp/reverse/", values);
            req.getCachePolicy().setCacheLimitTimeMs(1000 * 30); // キャッシュ有効時間指定
            req.getCachePolicy().setMaxItemBytes(1024 * 1024); // キャッシュサイズ最大値指定

            NetworkResult<String> connect = connector.connect(req, StringParser.getInstance());
            assertNotNull(connect);
            assertNotNull(connect.getConnection());
            assertNotNull(connect.getTaskResult());

            String html = connect.await(CONNECT_WAIT_MS);
            assertFalse(StringUtil.isEmpty(html));

            if (i == 0) {
                // 初回通信時
                assertNull(connect.getCacheDigest());
                assertNotNull(connect.getContentDigest());
                assertTrue(connect.hasContent());
                assertTrue(connect.hasContent());
                oldValue = html;
            } else {
                assertNotNull(connect.getCacheDigest()); // キャッシュがあるので、!= null
                assertNull(connect.getContentDigest()); // キャッシュロードしたので、 == null
                assertTrue(connect.hasContent());
                newValue = html;
            }
        }

        assertEquals(oldValue, newValue);
    }

    public void test_simpleGetValues() throws Exception {
        NetworkConnector connector = newConnector();
        SimpleHttpRequest req = new SimpleHttpRequest(ConnectRequest.Method.GET);
        Map<String, String> values = new HashMap<>();
        values.put("lat", "35.68653");
        values.put("lon", "139.69193");
        req.setUrl("http://geocode.didit.jp/reverse/", values);

        NetworkResult<String> connect = connector.connect(req, StringParser.getInstance());
        assertNotNull(connect);
        assertNotNull(connect.getConnection());
        assertNotNull(connect.getTaskResult());

        String html = connect.await(CONNECT_WAIT_MS);
        assertFalse(StringUtil.isEmpty(html));
    }

    public void test_simpleGetValuesWithQ() throws Exception {
        NetworkConnector connector = newConnector();
        SimpleHttpRequest req = new SimpleHttpRequest(ConnectRequest.Method.GET);
        Map<String, String> values = new HashMap<>();
        values.put("lat", "35.68653");
        values.put("lon", "139.69193");
        req.setUrl("http://geocode.didit.jp/reverse/?", values);

        NetworkResult<String> connect = connector.connect(req, StringParser.getInstance());
        assertNotNull(connect);
        assertNotNull(connect.getConnection());
        assertNotNull(connect.getTaskResult());

        String html = connect.await(CONNECT_WAIT_MS);
        assertFalse(StringUtil.isEmpty(html));
    }

}
