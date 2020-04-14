package org.example.rest.route;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableAutoConfiguration
@RunWith(CamelSpringRunner.class)
@SpringBootTest(properties = { "remote.end.url=http://localhost:19090/hello" })
public class RestApiTest extends AbstractJUnit4SpringContextTests {

    // Wiremock stubbing
    WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(19090));

    @Autowired
    private CamelContext camelContext;

    @Produce(uri = "direct:remoteService")
    protected ProducerTemplate template;

    @Before
    public void setUp() {
        wireMockServer.start();
    }

    @After
    public void destroy() {
        wireMockServer.stop();
    }

    @Test
    public void testRestEndpoint() throws Exception {

        // Set up
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("Content-Type", "application/json");
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", 1);
        payload.put("message", "test hello");
        exchange.getIn().setBody(payload);

        // wiremock setup
        InputStream mockPayloadInputStream = this.getClass().getClassLoader().getResourceAsStream("mockResponse/hello.xml");
        String responseMockPayload = IOUtils.toString(mockPayloadInputStream);
        wireMockServer.stubFor(WireMock.post("/hello")
                .willReturn(WireMock.okXml(responseMockPayload)));

        // when
        Exchange response = template.send(exchange);
        Map<String, String> responseData = (Map<String, String>)response.getIn().getBody();

        // then
        Assert.assertEquals("hello world Back", responseData.get("response-msg"));
        Assert.assertEquals("success", responseData.get("status"));
    }

    @Test
    public void test404HttpStatus() throws Exception {
        // Set up
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("Content-Type", "application/json");
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", 1);
        payload.put("message", "test hello");
        exchange.getIn().setBody(payload);

        // wiremock setup
        wireMockServer.stubFor(WireMock.post("/hello")
                .willReturn(WireMock.notFound()));

        // when
        Exchange response = template.send(exchange);
        Map<String, String> responseData = (Map<String, String>)response.getIn().getBody();

        // then
        Assert.assertEquals("Error Status returned.", responseData.get("response-msg"));
        Assert.assertEquals("failed", responseData.get("status"));
    }
}
