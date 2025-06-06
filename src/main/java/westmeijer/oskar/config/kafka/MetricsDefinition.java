package westmeijer.oskar.config.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsDefinition {
  // TODO: put this in the Steps configuration

  public static final String PRODUCT_RECEIVED = "product.received";
  public static final String PRODUCT_RECEIVED_ERROR = "product.received.error";

  public static final String CATEGORY_ASSIGNED = "category.assigned";
  public static final String CATEGORY_ERROR = "category.error";

  public static final String PRICE_ASSIGNED = "price.assigned";
  public static final String PRICE_ERROR = "price.error";

  public static final String STOCK_ASSIGNED = "stock.assigned";
  public static final String STOCK_ERROR = "stock.error";

  public static final String PRODUCT_FINALIZED = "product.finalized";
  public static final String PRODUCT_FINALIZED_ERROR = "product.finalized.error";


  private final MeterRegistry meterRegistry;

  @PostConstruct
  void init() {
    meterRegistry.counter(STOCK_ASSIGNED).count();
    meterRegistry.counter(STOCK_ERROR).count();
    meterRegistry.counter(CATEGORY_ASSIGNED).count();
    meterRegistry.counter(CATEGORY_ERROR).count();
    meterRegistry.counter(PRICE_ASSIGNED).count();
    meterRegistry.counter(PRICE_ERROR).count();
  }

}
