package com.mx.stock.util;

import com.mx.stock.util.handler.ResponseHandler;
import com.mx.stock.util.handler.impl.DefaultResponseHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

/**
 * http请求模板工具类
 * @author xu.ma
 * @version 1.0.0
 * @date 2020/10/29 15:47
 */
@Slf4j
@Component
public class HttpTemplate {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 发送get请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理, 例如urlEncode, urlDecode
     * @param url   调用链接
     * @return  响应体, 响应状态码非2xx返回null
     */
    public String sendHttpGetString(String url) {
        return sendHttpGetString(url, null, null);
    }

    /**
     * 发送get请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理, 例如urlEncode, urlDecode
     * @param url   调用链接
     * @param headers    请求头
     * @return  响应体, 响应状态码非2xx返回null
     */
    public String sendHttpGetString(String url, HttpHeaders headers) {
        return sendHttpGetString(url, null, headers);
    }

    /**
     * 发送get请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理, 例如urlEncode, urlDecode
     * @param url   调用链接
     * @param requestBody   url链接里的占位符与值的pair对象
     * @param requestHeaders    请求头
     * @return 响应体, 响应状态码非2xx返回null
     */
    public <E> String sendHttpGetString(String url, E requestBody, HttpHeaders requestHeaders) {
        return sendHttpGet(url, requestBody, requestHeaders, String.class, new DefaultResponseHandler());
    }

    /**
     * 发送get请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理, 例如urlEncode, urlDecode
     * @param url   调用链接
     * @param requestBody   url链接里的占位符与值的pair对象
     * @param requestHeaders    请求头
     * @param clazz 返回的实体类型
     * @param responseHandler   响应处理器
     * @param <E>   Request body返回的参数类型
     * @param <T>   ResponseEntity返回的参数类型
     * @param <R>   handler处理后的参数返回类型
     * @return 处理后的响应返回
     */
    @SneakyThrows
    public <E, T, R> R sendHttpGet(String url, E requestBody, HttpHeaders requestHeaders, Class<T> clazz, ResponseHandler<T, R> responseHandler) {
        return sendHttp(new URI(url), HttpMethod.GET, requestBody, requestHeaders, clazz, responseHandler);
    }

    /**
     * 发送get请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理, 例如urlEncode, urlDecode
     * @param url   调用链接
     * @param requestBody   url链接里的占位符与值的pair对象
     * @param requestHeaders    请求头
     * @param clazz 返回的实体类型
     * @param <E>   Request body返回的参数类型
     * @param <T>   Response body返回的参数类型
     * @return  responseBody为T类型的response
     */
    @SneakyThrows
    public <E, T> ResponseEntity<T> sendHttpGet(String url, E requestBody, HttpHeaders requestHeaders, Class<T> clazz) {
        return sendHttp(new URI(url), HttpMethod.GET, requestBody, requestHeaders, clazz);
    }

    /**
     * 发送post请求
     * @param url   调用链接
     * @return  响应体, 响应状态码非2xx返回null
     */
    public String sendHttpPostString(String url) {
        return sendHttpPostString(url, null);
    }

    /**
     * 发送post请求
     * 如果requestBody是字符串, Content-Type=application/json;charset=UTF-8
     * 否则Content-Type=application/x-www-form-urlencoded
     * @param url   调用链接
     * @param requestBody   请求的requestBody
     * @param <E> requestBody的类型
     * @return  响应体, 响应状态码非2xx返回null
     */
    public <E> String sendHttpPostString(String url, E requestBody) {

        return sendHttpPostString(url, requestBody, null);
    }

    /**
     * 发送post请求
     * 如果不显式声明Content-Type, 使用默认Content-Type:
     * 如果requestBody是字符串, Content-Type=application/json;charset=UTF-8,
     * 否则Content-Type=application/x-www-form-urlencoded
     * @param url   调用链接
     * @param requestBody   请求的requestBody
     * @param requestHeaders    请求头
     * @param <E> requestBody的请求类型
     * @return  响应体, 响应状态码非2xx返回null
     */
    public <E> String sendHttpPostString(String url, E requestBody, HttpHeaders requestHeaders) {
        return sendHttpPost(url, requestBody, requestHeaders, String.class, new DefaultResponseHandler());
    }

    /**
     * 发送post请求
     * 如果不显式声明Content-Type, 使用默认Content-Type:
     * 如果requestBody是字符串, Content-Type=application/json;charset=UTF-8,
     * 否则Content-Type=application/x-www-form-urlencoded
     * @param url   调用链接
     * @param requestBody   请求的requestBody
     * @param requestHeaders    请求头
     * @param responseHandler   响应处理器
     * @param <E> requestBody的请求类型
     * @return  响应体, 响应状态码非2xx返回null
     */
    public <E> String sendHttpPostString(String url, E requestBody, HttpHeaders requestHeaders, ResponseHandler<String, String> responseHandler) {
        return sendHttpPost(url, requestBody, requestHeaders, String.class, responseHandler);
    }

    /**
     * 发送post请求
     * 如果不显式声明Content-Type, 使用默认Content-Type:
     * 如果requestBody是字符串, Content-Type=application/json;charset=UTF-8,
     * 否则Content-Type=application/x-www-form-urlencoded
     * @param url   调用链接
     * @param requestBody   请求的requestBody
     * @param requestHeaders    请求头
     * @param clazz 返回的实体类型
     * @param responseHandler   响应处理器
     * @param <E>   requestBody的请求类型
     * @param <T>   ResponseEntity返回的参数类型
     * @param <R>   handler处理后的参数返回类型
     * @return  处理后的响应返回
     */
    public <E, T, R> R sendHttpPost(String url, E requestBody, HttpHeaders requestHeaders, Class<T> clazz, ResponseHandler<T, R> responseHandler) {
        HttpHeaders headers = makeDefaultContentType(requestHeaders, requestBody);
        if(MediaType.APPLICATION_FORM_URLENCODED.equals(headers.getContentType())) {
            Object body = makeRequestBody(requestBody);
            return sendHttp(url, HttpMethod.POST, body, headers, clazz, responseHandler);
        }
        return sendHttp(url, HttpMethod.POST, requestBody, headers, clazz, responseHandler);
    }

    /**
     * 发送post请求
     * 如果不显式声明Content-Type, 使用默认Content-Type:
     * 如果requestBody是字符串, Content-Type=application/json;charset=UTF-8,
     * 否则Content-Type=application/x-www-form-urlencoded
     * @param url   调用链接
     * @param requestBody   请求的requestBody
     * @param requestHeaders    请求头
     * @param clazz 返回的实体类型
     * @param <E>   Request body的参数类型
     * @param <T>   Response body返回的参数类型
     * @return  responseBody为T类型的response
     */
    public <E, T> ResponseEntity<T> sendHttpPost(String url, E requestBody, HttpHeaders requestHeaders, Class<T> clazz) {
        HttpHeaders headers = makeDefaultContentType(requestHeaders, requestBody);
        if(MediaType.APPLICATION_FORM_URLENCODED.equals(headers.getContentType())) {
            Object body = makeRequestBody(requestBody);
            return sendHttp(url, HttpMethod.POST, body, headers, clazz);
        }
        return sendHttp(url, HttpMethod.POST, requestBody, headers, clazz);
    }

    /**
     * 发送http请求, url连接里的参数<strong>会</strong>被restTemplate二次处理
     * @param url   调用链接
     * @param httpMethod    GET,POST,PUT,DELETE
     * @param requestBody   请求的requestBody
     * @param headers    请求头
     * @param clazz 返回的实体类型
     * @param responseHandler   响应处理器
     * @param <E>   requestBody的实体类型
     * @param <T>   ResponseEntity返回的参数类型
     * @param <R>   handler处理后的参数返回类型
     * @return  处理后的响应返回
     */
    public <E, T, R> R sendHttp(String url, HttpMethod httpMethod, E requestBody, HttpHeaders headers, Class<T> clazz, ResponseHandler<T, R> responseHandler) {
        ResponseEntity<T> responseEntity = sendHttp(url, httpMethod, requestBody, headers, clazz);
        return responseHandler.handle(responseEntity);
    }

    /**
     * 发送http请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理
     * @param url   调用链接
     * @param httpMethod    GET,POST,PUT,DELETE
     * @param requestBody   请求的requestBody
     * @param headers    请求头
     * @param clazz 返回的实体类型
     * @param responseHandler   响应处理器
     * @param <E>   requestBody的实体类型
     * @param <T>   ResponseEntity返回的参数类型
     * @param <R>   handler处理后的参数返回类型
     * @return  处理后的响应返回
     */
    public <E, T, R> R sendHttp(URI url, HttpMethod httpMethod, E requestBody, HttpHeaders headers, Class<T> clazz, ResponseHandler<T, R> responseHandler) {
        ResponseEntity<T> responseEntity = sendHttp(url, httpMethod, requestBody, headers, clazz);
        return responseHandler.handle(responseEntity);
    }

    /**
     * 发送http请求, url连接里的参数<strong>会</strong>被restTemplate二次处理
     * @param url   调用链接
     * @param httpMethod    GET,POST,PUT,DELETE
     * @param requestBody   请求的requestBody
     * @param requestHeaders    请求头
     * @param clazz 返回的实体类型
     * @param <E>   requestBody的实体类型
     * @param <T>   返回的实体类型
     * @return  responseBody为T类型的response
     */
    public <E, T> ResponseEntity<T> sendHttp(String url, HttpMethod httpMethod, E requestBody, HttpHeaders requestHeaders, Class<T> clazz) {
        HttpEntity<E> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        return restTemplate.exchange(url, httpMethod, requestEntity, clazz);
    }

    /**
     * 发送http请求, url连接里的参数<strong>不会</strong>被restTemplate二次处理
     * @param url   调用链接uri
     * @param httpMethod    GET,POST,PUT,DELETE
     * @param requestBody   请求的requestBody
     * @param requestHeaders    请求头
     * @param clazz 返回的实体类型
     * @param <E>   requestBody的实体类型
     * @param <T>   返回的实体类型
     * @return  responseBody为T类型的response
     */
    public <E, T> ResponseEntity<T> sendHttp(URI url, HttpMethod httpMethod, E requestBody, HttpHeaders requestHeaders, Class<T> clazz) {
        HttpEntity<E> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        return restTemplate.exchange(url, httpMethod, requestEntity, clazz);
    }

    /**
     * 把Map对象转化为HttpHeaders对象
     * requestHeaders的转化
     * @param headerMap
     * @return  转化的HttpHeaders对象
     */
    public HttpHeaders buildHttpHeaders(Map<String, ?> headerMap) {

        if(headerMap == null || headerMap.size() == 0) {
            return null;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        for(Map.Entry<String, ?> entry : headerMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if(StringUtils.isNotEmpty(key) && value != null) {
                httpHeaders.add(key, value.toString());
            }
        }

        return httpHeaders;
    }

    /**
     *
     * 设置默认的Content-Type为application/json;charset=UTF-8
     * @param requestHeaders    原有的请求头
     * @param requestBody 请求体
     * @param <E> 请求体类型
     * @return  请求头
     * @see HttpHeaders
     * @see MediaType
     */
    private <E> HttpHeaders makeDefaultContentType(HttpHeaders requestHeaders, E requestBody) {

        if(requestBody == null) {
            return requestHeaders;
        }

        boolean isString = requestBody instanceof String;

        if(requestHeaders == null) {
            requestHeaders = new HttpHeaders();
            if(isString) {
                requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            } else {
                requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            }
        } else {
            if(requestHeaders.getContentType() == null) {
                if(isString) {
                    requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                } else {
                    requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                }
            }
        }
        return requestHeaders;
    }

    private <E> Object makeRequestBody(E requestBody) {

        if(requestBody == null || requestBody instanceof LinkedMultiValueMap) {
            return requestBody;
        }

        LinkedMultiValueMap<String, String> linkedMultiValueMap = null;
        //转化
        if(requestBody instanceof Map) {

            linkedMultiValueMap = new LinkedMultiValueMap<>();
            //map转化
            Map<String, Object> requestBodyMap = (Map<String, Object>) requestBody;
            for(Map.Entry<String, Object> entry : requestBodyMap.entrySet()) {

                String key = entry.getKey();
                Object value = entry.getValue();
                if(StringUtils.isNotEmpty(key) && value != null) {
                    linkedMultiValueMap.add(key, value.toString());
                }
            }

        } else if(requestBody instanceof Serializable) {
            //实体对象转化
            linkedMultiValueMap = entity2ValueMap(requestBody);
        }

        if(linkedMultiValueMap == null || linkedMultiValueMap.size() == 0) {
            return requestBody;
        } else {
            return linkedMultiValueMap;
        }
    }

    private LinkedMultiValueMap<String, String> entity2ValueMap(Object object) {
        PropertyDescriptor[] props = null;
        LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<>();
        try {
            props = Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors();
            if (props != null) {
                for (PropertyDescriptor prop : props) {
                    String name = prop.getName();
                    Method readMethod = prop.getReadMethod();
                    readMethod.setAccessible(true);
                    Object value = readMethod.invoke(object);
                    if (StringUtils.isNotEmpty(name) && value != null) {
                        linkedMultiValueMap.add(name, value.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("http error: ", e);
        }
        return linkedMultiValueMap;
    }
}
