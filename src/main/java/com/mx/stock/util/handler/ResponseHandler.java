package com.mx.stock.util.handler;

import org.springframework.http.ResponseEntity;

/**
 * ResponseEntity自定义转换接口
 * @author xu.ma
 * @version 1.0.0
 * @date 2020/10/29 17:24
 * @see com.mx.stock.util.handler.impl.DefaultResponseHandler
 */
@FunctionalInterface
public interface ResponseHandler<T, R> {

    /**
     * 对response entity的实体处理
     * @param responseEntity    请求返回的response entity
     * @return  处理后的值
     */
    R handle(ResponseEntity<T> responseEntity);
}
