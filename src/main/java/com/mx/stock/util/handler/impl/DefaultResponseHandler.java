package com.mx.stock.util.handler.impl;

import com.mx.stock.util.handler.ResponseHandler;
import org.springframework.http.ResponseEntity;

/**
 * the default response handler
 * @author xu.ma
 * @version 1.0.0
 * @date 2020/10/29 20:33
 */
public class DefaultResponseHandler implements ResponseHandler<String, String> {

    @Override
    public String handle(ResponseEntity<String> responseEntity) {

        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return null;
    }
}
