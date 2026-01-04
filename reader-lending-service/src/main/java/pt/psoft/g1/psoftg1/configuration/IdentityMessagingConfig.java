package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityMessagingConfig {

  @Bean
  public DirectExchange identityExchange(@Value("${identity.exchange}") String name) {
    return new DirectExchange(name);
  }

  // Jackson (JSON) serialization for records
  @Bean
  public MessageConverter jacksonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory cf,
      MessageConverter converter,
      @Value("${identity.rpc.timeout-ms}") long timeoutMs) {
    RabbitTemplate template = new RabbitTemplate(cf);
    template.setMessageConverter(converter);
    template.setReplyTimeout(timeoutMs);
    return template;
  }
}
