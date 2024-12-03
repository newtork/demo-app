package org.demo;
import com.sap.ai.sdk.orchestration.OrchestrationClient;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.sap.ai.sdk.orchestration.OrchestrationAiModel.GPT_4O;
import static com.sap.ai.sdk.orchestration.OrchestrationAiModel.Parameter.TEMPERATURE;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @Bean
  OrchestrationClient getOrchestrationClient() {
    return new OrchestrationClient();
  }

  @Bean
  OrchestrationModuleConfig getOrchestrationConfig() {
    var config = new OrchestrationModuleConfig();
    return config.withLlmConfig(GPT_4O.withParam(TEMPERATURE, 0.5));
  }
}
