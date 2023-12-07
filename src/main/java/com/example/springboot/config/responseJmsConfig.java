package com.example.springboot.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class responseJmsConfig {

    @Bean
    public ActiveMQConnectionFactory responseActiveMQConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("https://sqs.us-gov-east-1.amazonaws.com/539901372416/gc-poc-ves-outbound-dev-response-queue");
        factory.setPassword("admin");
        factory.setUserName("admin");
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return new JmsTemplate(responseActiveMQConnectionFactory());
    }

    // Configure second connection factory if needed
}