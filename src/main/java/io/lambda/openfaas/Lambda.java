// Copyright (c) OpenFaaS Author(s) 2018. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full
// license information.

package io.lambda.openfaas;

import io.lambda.openfaas.model.IRequest;
import io.lambda.openfaas.model.IResponse;

/**
 * Lambda function contract to implement
 */
public interface Lambda {

    /**
     * @param request to process
     * @return output if specified type
     */
    IResponse handle(IRequest request);
}
