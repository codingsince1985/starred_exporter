package com.j5ee.starred.export;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelMain {

    public static void main(String args[]) throws Exception {
        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").process(new SmtpProcessor()).end();
            }
        });

        context.start();
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:start", "");

        context.stop();
    }
}
