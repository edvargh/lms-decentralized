package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityAmqpConfig {

  @Bean
  public DirectExchange identityExchange(@Value("${identity.exchange}") String name) {
    return new DirectExchange(name);
  }

  @Bean
  public Queue createUserQueue(@Value("${identity.create-user.queue}") String name) {
    return QueueBuilder.durable(name).build();
  }

  @Bean
  public Queue deleteUserQueue(@Value("${identity.delete-user.queue}") String name) {
    return QueueBuilder.durable(name).build();
  }

  @Bean
  public Binding bindCreate(Queue createUserQueue, DirectExchange ex,
      @Value("${identity.create-user.routing-key}") String rk) {
    return BindingBuilder.bind(createUserQueue).to(ex).with(rk);
  }

  @Bean
  public Binding bindDelete(Queue deleteUserQueue, DirectExchange ex,
      @Value("${identity.delete-user.routing-key}") String rk) {
    return BindingBuilder.bind(deleteUserQueue).to(ex).with(rk);
  }

  @Bean
  public MessageConverter jacksonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      MessageConverter messageConverter) {

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    return factory;
  }

}
