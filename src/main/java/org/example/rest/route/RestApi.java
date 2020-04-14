package org.example.rest.route;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.example.rest.processor.HttpClientProcessor;
import org.example.rest.processor.ResponseProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.ws.rs.core.MediaType;
import java.util.Arrays;



@Component
public class RestApi extends DefaultRestApiEndpointConfig {

    @Value("${example.api.context.path}")
    private String apiContextPath;

    @Autowired
    private HttpClientProcessor httpClientProcessor;
    @Autowired
    private ResponseProcessor responseProcessor;


    @Override
    public void configure() throws Exception {

        super.configure();

        // Data Format
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);
        xmlJsonFormat.setRootName("Hello");
        xmlJsonFormat.setSkipNamespaces(true);
        xmlJsonFormat.setRemoveNamespacePrefixes(true);
        xmlJsonFormat.setExpandableProperties(Arrays.asList("d", "e"));

        rest("/api/")
                .id("api-route")
                .consumes("application/json")
                .post(apiContextPath)
                .produces(MediaType.APPLICATION_JSON)
                .consumes(MediaType.APPLICATION_JSON)
                .to("direct:remoteService");

        from("direct:remoteService")
                .routeId("direct-remoteServoce")
                .log(LoggingLevel.INFO, ">>> ${body}")
                .marshal().json(JsonLibrary.Jackson)
                .log(LoggingLevel.INFO, ">>> String payload: ${body}")
                .to("json-validator:schema/helloWorld.json")
                .log(LoggingLevel.DEBUG,">>> Validated payload: ${body}")
                .unmarshal(xmlJsonFormat)
                .log(LoggingLevel.DEBUG, ">>> xml payload: ${body}")
                .to("xslt:xslt/HelloWorldRequest.xslt.xsl")
                .log(LoggingLevel.DEBUG, ">>> xml transformed payload: ${body}")
                .to("validator:schema/helloServer.xsd")
                .log(LoggingLevel.INFO, ">>> String payload: ${body}")
                .process(httpClientProcessor)
                // or one can use the following:
                //.removeHeader(Exchange.HTTP_PATH)
                //.removeHeaders("CamelHttp*")
                //.setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                //.setHeader("Content-Type",constant("application/xml"))
                //.to("http://localhost:9090/hello")
                // need to update choice to look at http response code.
                .choice()
                    .when(header(HttpClientProcessor.RESPONSE_HTTP_STATUS).isEqualTo(200))
                        .log(LoggingLevel.DEBUG, "Successful Response returned: ${body}")
                        .setHeader(ResponseProcessor.RESPONSE_MSG, xpath("/HelloServer/ResMsg", String.class))
                    .otherwise()
                        .setHeader(ResponseProcessor.RESPONSE_MSG, constant("Error Status returned."))
                .end()
                .log(LoggingLevel.DEBUG, ">>> String payload: ${body}")
                .process(responseProcessor)
                .removeHeader(ResponseProcessor.RESPONSE_MSG)
                .log(LoggingLevel.INFO, ">>> String payload: ${body}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));


    }
}
