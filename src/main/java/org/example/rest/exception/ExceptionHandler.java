package org.example.rest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jsonvalidator.JsonValidationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExceptionHandler implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        Map<String, String> errorMsg = new HashMap<>();
        if (exception instanceof JsonValidationException) {
            errorMsg.put("status", "client-error");
        } else if (exception instanceof  ApplicationCustomProcessorException) {
            errorMsg.put("status", "server-error");
        } else {
            errorMsg.put("status", "unknown-server-error");
        }
        errorMsg.put("error-msg", exception.getMessage());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(errorMsg);
        exchange.getOut().setBody(jsonString);
    }
}
