package org.example.rest.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.example.rest.exception.ApplicationCustomProcessorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.InputStream;

@Component
public class HttpClientProcessor implements Processor {

    public static final String RESPONSE_HTTP_STATUS = "Response-Http-Status";

    @Value("${remote.end.url}")
    private String endUrl;

    @Override
    public void process(Exchange exchange) throws Exception {
        String payload = exchange.getIn().getBody(String.class);
        byte[] bytes = payload.getBytes();
        System.out.println("xml: " + payload);
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());
        client.getParams().setSoTimeout(5000);
        client.getParams().setConnectionManagerTimeout(5000);
        Header header = new Header("Content-Type","application/xml");
        PostMethod postMethod = new PostMethod(endUrl);
        postMethod.setRequestHeader(header);
        RequestEntity postReqEntity = new ByteArrayRequestEntity(bytes);
        postMethod.setRequestEntity(postReqEntity);
        try {
            int response = client.executeMethod(postMethod);
            InputStream respondBody = postMethod.getResponseBodyAsStream();
            byte[] responseBytes = IOUtils.toByteArray(respondBody);
            String responseStr = new String(responseBytes);
            exchange.getOut().setBody(responseStr);
            exchange.getOut().setHeader(RESPONSE_HTTP_STATUS, response);
        } catch (Exception io) {
            throw new ApplicationCustomProcessorException("HttpClientProcessor Error", io);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
