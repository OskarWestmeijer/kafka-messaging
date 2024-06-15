package westmeijer.oskar.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import westmeijer.oskar.model.Product;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductsCEStructuredConsumer {

  @Getter
  private Product latestMsg;

  private final Validator validator;

  private final ObjectMapper objectMapper;

  private final MeterRegistry meterRegistry;

  @KafkaListener(topics = "${kafka.servers.products.consumers.products-ce-structured.topic-name}")
  public void listenToCEStructuredProducts(ConsumerRecord<String, CloudEvent> message) {
    log.info("Received message from products-ce-structured topic. key: {}, value: {}, message: {}", message.key(), message.value(),
        message);
    var validationErrors = validator.validate(message.value());
    if (!validationErrors.isEmpty()) {
      log.error("Message had validation errors! errors: {}", validationErrors);
      throw new IllegalArgumentException(validationErrors.toString());
    }

    var productCE = message.value();
    log.info("Received cloud event: {}", productCE);
    meterRegistry.counter("products-ce-structured.consumed").increment();
  }

}
