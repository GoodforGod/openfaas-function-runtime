package io.lambda.openfaas;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.lambda.openfaas.convert.Converter;
import io.lambda.openfaas.error.HttpException;
import io.lambda.openfaas.logger.LambdaLogger;
import io.lambda.openfaas.model.IRequest;
import io.lambda.openfaas.model.IResponse;
import io.lambda.openfaas.model.Request;
import io.lambda.openfaas.model.Response;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.11.2020
 */
@Introspected
@Singleton
public class OpenfaasHttpHandler implements HttpHandler {

    private final LambdaLogger logger;
    private final Lambda lambda;
    private final Converter converter;

    @Inject
    public OpenfaasHttpHandler(LambdaLogger logger, Lambda lambda, Converter converter) {
        this.logger = logger;
        this.lambda = lambda;
        this.converter = converter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = "";
        final long reqStart = getTime();
        logger.debug("Starting request processing...");
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try (final InputStream inputStream = exchange.getRequestBody()) {
                try (final ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1)
                        result.write(buffer, 0, length);

                    requestBody = result.toString(StandardCharsets.UTF_8);
                }
            }
        }

        final Map<String, String> headers = exchange.getRequestHeaders().entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
        logger.debug("Headers: %s", headers);

        final String query = exchange.getRequestURI().getRawQuery();
        final IRequest request = new Request(requestBody, headers, query, exchange.getRequestURI().getPath());
        logger.debug("Request processing took: %s", timeSpent(reqStart));

        final long funcStart = getTime();
        logger.debug("Starting function handling...");
        IResponse response;
        try {
            response = lambda.handle(request);
            logger.info("Function handling took: %s", timeSpent(funcStart));
        } catch (Exception e) {
            logger.error("Function handling failed with: %s", e.getMessage());
            final int status = (e instanceof HttpException) ? ((HttpException) e).getCode() : 500;
            final String body = getErrorResponse(e);
            response = new Response().setContentType("application/json").setStatusCode(status).setBody(body);
        }

        final long resStart = getTime();
        logger.debug("Starting response processing...");

        final Headers responseHeaders = exchange.getResponseHeaders();
        if (StringUtils.isNotEmpty(response.getContentType()))
            responseHeaders.set("Content-Type", response.getContentType());
        response.getHeaders().forEach(responseHeaders::set);

        final byte[] bytesOut = response.getBody() instanceof String
                ? ((String) response.getBody()).getBytes(StandardCharsets.UTF_8)
                : converter.convertToJson(response.getBody()).getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(response.getStatusCode(), bytesOut.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytesOut);
        }

        logger.info("Response processing took: %s", timeSpent(resStart));
    }

    private static String getErrorResponse(Throwable e) {
        return String.format("{\"errorMessage\":\"%s\", \"errorType\":\"%s\"}",
                e.getMessage(), e.getClass().getSimpleName());
    }

    private static long getTime() {
        return System.currentTimeMillis();
    }

    private static String timeSpent(long started) {
        return (System.currentTimeMillis() - started) + " millis";
    }
}
