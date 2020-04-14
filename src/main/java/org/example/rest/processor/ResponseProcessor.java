package org.example.rest.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseProcessor implements Processor {

    public static final String RESPONSE_MSG = "response-message";

    @Override
    public void process(Exchange exchange) throws Exception {
        String resMsg = (String)exchange.getIn().getHeader(RESPONSE_MSG);
        Map<String, String> response = new HashMap<>();
        if (resMsg.contains("Error")) {
            response.put("status", "failed");
        } else {
            response.put("status", "success");
        }
        response.put("response-msg", resMsg);
        exchange.getOut().setBody(response);
    }
}
