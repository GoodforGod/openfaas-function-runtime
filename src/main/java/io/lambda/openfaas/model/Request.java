// Copyright (c) OpenFaaS Author(s) 2018. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full
// license information.

package io.lambda.openfaas.model;

import io.micronaut.core.annotation.Introspected;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Introspected
public class Request implements IRequest {

    private final Map<String, String> headers;
    private final Map<String, String> queryParameters;
    private final String body;
    private final String queryRaw;
    private final String pathRaw;
    private final Map<String, String> path;

    public Request(String body, Map<String, String> headers, String queryRaw, String path) {
        this.body = body;
        this.headers = headers;
        this.queryRaw = queryRaw;
        this.queryParameters = this.parseQueryParameters();
        this.pathRaw = path;
        this.path = this.parsePathParameters();
    }

    public String getBody() {
        return this.body;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    @Override
    public String getQueryRaw() {
        return queryRaw;
    }

    @Override
    public Map<String, String> getQuery() {
        return queryParameters;
    }

    @Override
    public String getPathRaw() {
        return pathRaw;
    }

    @Override
    public Map<String, String> getPath() {
        return path;
    }

    private Map<String, String> parsePathParameters() {
        final Map<String, String> res = new HashMap<>();
        if (pathRaw != null && pathRaw.length() > 0) {
            String[] params = pathRaw.substring(1).split("/");
            String key = "";
            for (String param : params) {
                if (key.length() > 0) {
                    res.put(key, param);
                    key = "";
                } else {
                    key = param;
                }
            }
            if (key.length() > 0) {
                res.put(key, "");
            }
        }

        return res;
    }

    private Map<String, String> parseQueryParameters() {
        final Map<String, String> reqParametersMap = new HashMap<>();
        if (queryRaw != null) {
            String pairs[] = queryRaw.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                try {
                    if (param.length > 0)
                        key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                    if (param.length > 1)
                        value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));

                    reqParametersMap.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return reqParametersMap;
    }

}
