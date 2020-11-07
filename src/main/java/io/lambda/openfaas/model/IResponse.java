// Copyright (c) OpenFaaS Author(s) 2018. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full
// license information.

package io.lambda.openfaas.model;

import java.util.Map;

public interface IResponse {

    Object getBody();

    String getHeader(String key);

    Map<String, String> getHeaders();

    String getContentType();

    int getStatusCode();
}
