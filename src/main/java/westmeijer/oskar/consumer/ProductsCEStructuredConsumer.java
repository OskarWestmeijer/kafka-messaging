package westmeijer.oskar.consumer;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import westmeijer.oskar.config.kafka.MetricsDefinition;
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

  @KafkaListener(topics = "${kafka.servers.products.consumers.products-ce-structured.topic-name}",
      containerFactory = "productsCEStructuredContainerFactory")
  public void listenToCEStructuredProducts(ConsumerRecord<String, CloudEvent> message) {
    log.info("Received message from products-ce-structured topic. key: {}, value: {}, message: {}", message.key(), message.value(),
        message);

    PojoCloudEventData<Product> deserializedData = CloudEventUtils
        .mapData(message.value(), PojoCloudEventDataMapper.from(objectMapper, Product.class));
    requireNonNull(deserializedData, "message data is required");
    var product = deserializedData.getValue();

    var validationErrors = validator.validate(product);
    if (!validationErrors.isEmpty()) {
      log.error("Message had validation errors! errors: {}", validationErrors);
      throw new IllegalArgumentException(validationErrors.toString());
    }

    latestMsg = product;
    log.info("Deserialized structured cloud event. value: {}", product);
    meterRegistry.counter(MetricsDefinition.PRODUCTS_CE_STRUCTURED_CONSUMED).increment();
  }

  public void clearLastMessage() {
    latestMsg = null;
  }

}
