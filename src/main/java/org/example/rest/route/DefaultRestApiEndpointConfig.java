package org.example.rest.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.example.rest.exception.ApplicationCustomProcessorException;
import org.example.rest.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;



public abstract class DefaultRestApiEndpointConfig extends RouteBuilder {

    @Value("${server.port}")
    String serverPort;
    @Value("${example.api.path}")
    private String contextPath;
    @Autowired
    private ExceptionHandler exceptionHandler;

    @Override
    public void configure() throws Exception {
        CamelContext context = new DefaultCamelContext();

        restConfiguration().contextPath(contextPath)
                .port(serverPort)
                .enableCORS(false)
                .apiContextPath("hello-api")
                .apiProperty("api.title", "Hello Rest Api")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true") // cross-site
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        // Default exception handling and catch all exceptions
        onException(ApplicationCustomProcessorException.class, Exception.class)
                .log("cool Exception ========= \n\n\n")
                .handled(true)
                .process(exceptionHandler)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader("Content-Type", constant("application/json"))
        ;

        //error handler
        //errorHandler();
        //errorHandler(defaultErrorHandler());
        //errorHandler(defaultErrorHandler().maximumRedeliveries(3)
        //        .redeliveryDelay(1000).retriesExhaustedLogLevel(LoggingLevel.WARN));
    }
}
