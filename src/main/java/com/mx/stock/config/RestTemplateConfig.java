package com.mx.stock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class RestTemplateConfig implements EnvironmentAware {

    private final MediaType[] mediaTypes = new MediaType[]{
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_JSON_UTF8,
            MediaType.TEXT_HTML,
            MediaType.TEXT_PLAIN,
            MediaType.TEXT_XML,
            MediaType.APPLICATION_STREAM_JSON,
            MediaType.APPLICATION_ATOM_XML,
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_PDF,
    };

    private Environment environment;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 整个连接池的最大支持连接数
     * 参数http.request.time.out时间内从连接池获取不到链接会报下面错误:
     * Timeout waiting for connection from pool; nested exception is org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
     */
    private static final String MAX_TOTAL_CONNECTION = "http.max.connection";
    /**
     * 当前主机到目的主机的一个路由，主要作用在通过httpClient转发请求到不同的目的主机的连接数限制,是maxTotal的一个细分;
     * 比如: maxtTotal=400 defaultMaxPerRoute=200, 而我只连接到http://www.baidu.com时, 到这个主机的并发最多只有200；而不是400；
     * 而我连接到http://www.baidu.com和http://www.jd.com时, 到每个主机的并发最多只有200; 即加起来是400(但不能超过400); 所以起作用的设置是defaultMaxPerRoute
     * 参数http.request.time.out时间内从连接池获取不到链接会报下面错误:
     * Timeout waiting for connection from pool; nested exception is org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
     */
    private static final String MAX_PER_ROUTE_CONNECTION = "http.per.route.connection";
    /**
     * 和目的主机建立连接的超时时间
     */
    private static final String CONNECTION_TIME_OUT = "http.connection.time.out";
    /**
     * 从目的主机读取数据超时时间
     */
    private static final String READ_TIME_OUT = "http.read.time.out";
    /**
     * 重试次数
     */
    private static final String RETRY_TIMES = "http.retry.times";
    /**
     * 从PoolingHttpClientConnectionManager中获取连接超时时间
     * 连接池中获取连接的时间, 超过这个时间获取不到连接会报下面错误:
     * Timeout waiting for connection from pool; nested exception is org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
     */
    private static final String CONNECT_REQUEST_TIME_OUT = "http.request.time.out";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        // 使用 utf-8 编码集的 convert 替换默认的 convert（默认的 string convert 的编码集为"ISO-8859-1"）
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for(HttpMessageConverter<?> converter : messageConverters) {
            if(converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
            if(converter instanceof MappingJackson2HttpMessageConverter) {
                //不加会出现异常
                //Could not extract response: no suitable HttpMessageConverter found for response type [class]
                ((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(Arrays.asList(mediaTypes));
            }
        }

        return restTemplate;
    }


    @Bean
    public HttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        //连接池最大连接数
        poolingConnectionManager.setMaxTotal(getIntValue(MAX_TOTAL_CONNECTION, 10));
        //每个主机的并发
        int value = getIntValue(MAX_PER_ROUTE_CONNECTION, 0);
        if(value > 0) {
            poolingConnectionManager.setDefaultMaxPerRoute(value);
        }
        return poolingConnectionManager;
    }

    @Bean
    public HttpClientBuilder httpClientBuilder() {

        final int retryTimes = getIntValue(RETRY_TIMES, 0);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //设置HTTP连接管理器
        httpClientBuilder.setConnectionManager(poolingConnectionManager());

        if(retryTimes > 0) {
            httpClientBuilder.setRetryHandler((exception, executionCount, context) -> {
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                log.error("request: " + request.getRequestLine().toString() + ", retry times: " + executionCount + ", for exception: " + exception.getMessage() + "...");
                return executionCount < retryTimes;
            });
        }
        return httpClientBuilder;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClientBuilder().build());
        clientHttpRequestFactory.setConnectTimeout(getIntValue(CONNECTION_TIME_OUT, 10000));
        clientHttpRequestFactory.setReadTimeout(getIntValue(READ_TIME_OUT, 10000));
        clientHttpRequestFactory.setConnectionRequestTimeout(getIntValue(CONNECT_REQUEST_TIME_OUT, 10000));
        return clientHttpRequestFactory;

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected String getStringValue(String key) {
        return getStringValue(key, "");
    }

    protected String getStringValue(String key, String defaultValue) {
        String value =  environment.containsProperty(key) ? environment.getProperty(key) : defaultValue;
        log.error("http keyValue is: " + key + "<==>" + value);
        return value;
    }

    protected int getIntValue(String key, int defaultValue) {

        String value = getStringValue(key, String.valueOf(defaultValue));
        try {
            if(StringUtils.isNotEmpty(value)) {
                return Integer.parseInt(value);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            log.error("get key: " + key + " got an exception, use default value instead, the default value is: " + defaultValue);
            return defaultValue;
        }
    }
}