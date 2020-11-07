// Copyright (c) OpenFaaS Author(s) 2018. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full
// license information.

package io.lambda.openfaas.model;

import io.micronaut.core.annotation.Introspected;

import java.util.HashMap;
import java.util.Map;

@Introspected
public class Response implements IResponse {

    private int statusCode = 200;
    private Object body;
    private String contentType;
    private final Map<String, String> headers;

    public Response() {
        this.body = "";
        this.contentType = "application/json";
        this.headers = new HashMap<>();
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public Response setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public Object getBody() {
        return body;
    }

    public Response setBody(Object body) {
        this.body = body;
        return this;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public Response setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
}
