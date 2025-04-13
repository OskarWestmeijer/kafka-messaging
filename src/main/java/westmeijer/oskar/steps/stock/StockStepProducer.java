package westmeijer.oskar.steps.stock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import westmeijer.oskar.service.model.Product;
import westmeijer.oskar.steps.StepProducer;
import westmeijer.oskar.steps.Steps;

@Slf4j
@Component
public class StockStepProducer implements StepProducer {

  private final Steps step = Steps.STOCK_ASSIGNMENT;
  private final KafkaTemplate<String, CloudEvent> binaryCloudEventsKafkaTemplate;
  private final ObjectMapper objectMapper;

  private final CloudEventBuilder ceEventTemplate = CloudEventBuilder.v1()
      .withSource(URI.create("https://oskar-westmeijer.com"))
      .withType("products-ce-structured")
      .withDataContentType("application/cloudevents+json");

  public StockStepProducer(
      @Qualifier(value = "binaryCloudEventsKafkaTemplate")
      KafkaTemplate<String, CloudEvent> binaryCloudEventsKafkaTemplate,
      ObjectMapper objectMapper) {
    this.binaryCloudEventsKafkaTemplate = binaryCloudEventsKafkaTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void produce(Product product) {
    Objects.requireNonNull(product);
    log.info("Producing to topic: {}, message: {}", step.outputTopic, product);
    String productJson;
    try {
      productJson = objectMapper.writeValueAsString(product);
    } catch (JsonProcessingException exception) {
      log.error("Error on serialization.");
      throw new RuntimeException(exception);
    }

    var productCE = ceEventTemplate
        .withId(UUID.randomUUID().toString())
        .withTime(OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Berlin")))
        .withData(productJson.getBytes(StandardCharsets.UTF_8))
        .build();

    binaryCloudEventsKafkaTemplate.send(step.outputTopic, String.valueOf(product.id()), productCE);
  }

}
