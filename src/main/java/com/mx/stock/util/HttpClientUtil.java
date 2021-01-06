package com.mx.stock.util;

/**
 * @author xu.ma
 * @version 1.0.0
 * @date 2021/1/6 13:26
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public final class HttpClientUtil {
    private static final String STR_ENCODE = "UTF-8";
    private static RequestConfig REQUEST_CONFIG;

    static {
        if (REQUEST_CONFIG == null) {
            REQUEST_CONFIG = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).setConnectionRequestTimeout(10000).build();
        }

    }

    private HttpClientUtil() {
    }

    public static String sendHttpPost(String httpUrl, Header... header) {
        HttpPost httpPost = new HttpPost(httpUrl);
        if (header != null) {
            httpPost.setHeaders(header);
        }

        return sendHttpPost(httpPost);
    }

    public static String sendHttpPost(String httpUrl, String params, Header... header) {
        HttpPost httpPost = new HttpPost(httpUrl);

        try {
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            stringEntity.setContentType("application/json;charset=UTF-8");
            httpPost.setEntity(stringEntity);
            if (header != null) {
                httpPost.setHeaders(header);
            }
        } catch (Exception var5) {
            throw new IllegalArgumentException(var5.getMessage());
        }

        return sendHttpPost(httpPost);
    }

    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Map<String, String> headers) {
        HttpPost httpPost = new HttpPost(httpUrl);
        List<NameValuePair> nameValuePairs = new ArrayList();
        Iterator var6 = maps.entrySet().iterator();

        while (var6.hasNext()) {
            Entry<String, String> entry = (Entry) var6.next();
            nameValuePairs.add(new BasicNameValuePair((String) entry.getKey(), (String) maps.get(entry.getKey())));
        }

        try {
            if (headers != null) {
                headers.forEach((n, k) -> {
                    httpPost.addHeader(new BasicHeader(n, k));
                });
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        } catch (Exception var7) {
            throw new IllegalArgumentException(var7.getMessage());
        }

        return sendHttpPost(httpPost);
    }

    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Header... header) {
        HttpPost httpPost = new HttpPost(httpUrl);
        List<NameValuePair> nameValuePairs = new ArrayList();
        Iterator var6 = maps.entrySet().iterator();

        while (var6.hasNext()) {
            Entry<String, String> entry = (Entry) var6.next();
            nameValuePairs.add(new BasicNameValuePair((String) entry.getKey(), (String) maps.get(entry.getKey())));
        }

        try {
            if (header != null) {
                httpPost.setHeaders(header);
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        } catch (Exception var7) {
            throw new IllegalArgumentException(var7.getMessage());
        }

        return sendHttpPost(httpPost);
    }

    public static String sendHttpFormPost(String url, Map<String, String> params) {
        HttpPost httpost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (Exception var7) {
            throw new IllegalArgumentException(var7.getMessage());
        }
        return sendHttpPost(httpost);
    }


    public static String sendHttpPost(String httpUrl, Map<String, String> maps) {
        HttpPost httpPost = new HttpPost(httpUrl);
        List<NameValuePair> nameValuePairs = new ArrayList();
        Iterator var5 = maps.entrySet().iterator();

        while(var5.hasNext()) {
            Entry<String, String> entry = (Entry)var5.next();
            nameValuePairs.add(new BasicNameValuePair((String)entry.getKey(), (String)maps.get(entry.getKey())));
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        } catch (Exception var6) {
            throw new IllegalArgumentException(var6.getMessage());
        }

        return sendHttpPost(httpPost);
    }

    public static String sendHttpPut(String httpUrl, Map<String, String> maps, Header... headers) {
        HttpPut httpPut = new HttpPut(httpUrl);
        if (headers != null) {
            httpPut.setHeaders(headers);
        }

        List<NameValuePair> nameValuePairs = new ArrayList();
        Iterator var6 = maps.entrySet().iterator();

        while(var6.hasNext()) {
            Entry<String, String> entry = (Entry)var6.next();
            nameValuePairs.add(new BasicNameValuePair((String)entry.getKey(), (String)maps.get(entry.getKey())));
        }

        try {
            httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        } catch (Exception var7) {
            throw new IllegalArgumentException(var7.getMessage());
        }

        return sendHttpRequest(httpPut);
    }

    public static String sendHttpPut(String httpUrl, String params, Header... header) {
        HttpPut httpPut = new HttpPut(httpUrl);

        try {
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            stringEntity.setContentType("application/json");
            httpPut.setEntity(stringEntity);
            if (header != null) {
                httpPut.setHeaders(header);
            }
        } catch (Exception var5) {
            throw new IllegalArgumentException(var5.getMessage());
        }

        return sendHttpRequest(httpPut);
    }

    public static String sendHttpDelete(String httpUrl, String params, Header... headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            httpHeaders.add("requestId", MDC.get("requestId"));
            httpHeaders.add("Content-Type", "application/json");
            Header[] var7 = headers;
            int var6 = headers.length;

            for(int var5 = 0; var5 < var6; ++var5) {
                Header header = var7[var5];
                httpHeaders.add(header.getName(), header.getValue());
            }
        }

        HttpEntity<String> httpEntity = new HttpEntity(params, httpHeaders);
        ResponseEntity<String> responseEntity = (new RestTemplate()).exchange(httpUrl, HttpMethod.DELETE, httpEntity, String.class, new Object[0]);
        return (String)responseEntity.getBody();
    }

    public static String sendHttpDelete(String httpUrl, Header... headers) {
        HttpDelete httpDelete = new HttpDelete(httpUrl);
        if (headers != null) {
            httpDelete.setHeaders(headers);
        }

        return sendHttpRequest(httpDelete);
    }

    public static String sendHttpPost(HttpPost httpPost) {
        return sendHttpRequest(httpPost);
    }

    public static String sendHttpGet(String httpUrl) {
        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpGet(httpGet);
    }

    public static String sendHttpGet(String httpUrl, Header... header) {
        HttpGet httpGet = new HttpGet(httpUrl);
        if (header != null) {
            httpGet.setHeaders(header);
        }

        return sendHttpGet(httpGet);
    }

    public static String sendHttpsGet(String httpUrl, Header... header) throws Exception {
        HttpGet httpGet = new HttpGet(httpUrl);
        if (header != null) {
            httpGet.setHeaders(header);
        }

        return sendHttpsGet(httpGet);
    }

    private static String sendHttpGet(HttpGet httpGet) {
        return sendHttpRequest(httpGet);
    }

    private static String sendHttpsGet(HttpGet httpGet) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        org.apache.http.HttpEntity entity = null;
        String responseContent = null;

        try {
            PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.load(new URL(httpGet.getURI().toString()));
            DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(publicSuffixMatcher);
            httpClient = HttpClients.custom().setSSLHostnameVerifier(hostnameVerifier).build();
            httpGet.setConfig(REQUEST_CONFIG);
            response = httpClient.execute(httpGet);
            entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception var14) {
            log.error("sendHttpsGet 异常:",var14);
            throw new IllegalArgumentException(var14.getMessage());
        } finally {
            try {
                closeResources(response, httpClient);
            } catch (IOException var13) {
                throw new IllegalArgumentException(var13.getMessage());
            }
        }

        return responseContent;
    }

    public static String sendHttpRequest(HttpRequestBase httpRequestBase) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        org.apache.http.HttpEntity entity = null;
        String responseContent = null;
        long start = System.currentTimeMillis();

        try {
            httpClient = HttpClients.createDefault();
            httpRequestBase.setConfig(REQUEST_CONFIG);
            response = httpClient.execute(httpRequestBase);
            entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception var15) {
            log.error("sendHttpsGet 异常:",var15);
            throw new IllegalArgumentException(var15.getMessage());
        } finally {
            try {
                closeResources(response, httpClient);
            } catch (IOException var14) {
                throw new IllegalArgumentException(var14.getMessage());
            }
        }

        log.info("httpclient [{}], cost time [{}] ms )", httpRequestBase.getURI().toString(), System.currentTimeMillis() - start);
        return responseContent;
    }

    private static void closeResources(CloseableHttpResponse closeableHttpResponse, CloseableHttpClient closeableHttpClient) throws IOException {
        if (closeableHttpResponse != null) {
            closeableHttpResponse.close();
        }

        if (closeableHttpClient != null) {
            closeableHttpClient.close();
        }

    }
}

