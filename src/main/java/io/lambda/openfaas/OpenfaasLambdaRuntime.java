package io.lambda.openfaas;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.lambda.openfaas.logger.LambdaLogger;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.annotation.Introspected;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.11.2020
 */
@Introspected
public class OpenfaasLambdaRuntime {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        final long contextStart = getTime();
        final ApplicationContextBuilder builder = ApplicationContext.builder().args(args);
        try (final ApplicationContext context = builder.build().start()) {
            final HttpHandler httpHandler = context.getBean(HttpHandler.class);
            final LambdaLogger logger = context.getBean(LambdaLogger.class);
            logger.debug("Context startup took: %s", timeSpent(contextStart));

            final long serverStart = getTime();
            logger.debug("Starting server...");
            final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/", httpHandler);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            server.setExecutor(executor);
            server.start();
            logger.debug("Server startup took: %s", timeSpent(serverStart));
            logger.info("Total startup took: %s", timeSpent(contextStart));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                executor.shutdown();
                executor.shutdownNow();
                context.stop();
                server.stop(0);
            }));
        }
    }

    private static long getTime() {
        return System.currentTimeMillis();
    }

    private static String timeSpent(long started) {
        return (System.currentTimeMillis() - started) + " millis";
    }
}
