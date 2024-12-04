package org.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.ai.sdk.orchestration.Message;
import com.sap.ai.sdk.orchestration.OrchestrationClient;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import com.sap.ai.sdk.orchestration.model.ChatMessage;
import com.sap.ai.sdk.orchestration.model.CompletionPostRequest;
import com.sap.ai.sdk.orchestration.model.LLMModuleConfig;
import com.sap.ai.sdk.orchestration.model.LLMModuleResultSynchronous;
import com.sap.ai.sdk.orchestration.model.ModuleConfigs;
import com.sap.ai.sdk.orchestration.model.OrchestrationConfig;
import com.sap.ai.sdk.orchestration.model.Template;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import static com.sap.ai.sdk.orchestration.OrchestrationAiModel.GPT_4O;
import static com.sap.ai.sdk.orchestration.OrchestrationAiModel.Parameter.TEMPERATURE;

@RestController
@RequestMapping("/chat")
public class ChatController {

  @Value("${chat.system.message}")
  String systemMessage;

  @GetMapping(value = "/step1", produces = "text/html")
  @Nonnull
  String completionVanilla(@RequestParam("text") String text) throws IOException {
    String HOST = null; // SECRET
    String DEPLOYMENT = null; // SECRET
    String RESOURCE_GROUP = null; // SECRET
    String AUTH_TOKEN = null; // SECRET

    // Create HttpClient
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      String url = "https://%s/v2/inference/deployments/%s/completion".formatted(HOST, DEPLOYMENT);
      HttpPost httpPost = new HttpPost(url);
      httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
      httpPost.setHeader("AI-Resource-Group", RESOURCE_GROUP);
      httpPost.setHeader("Authorization", "Bearer " + AUTH_TOKEN);

      String jsonPayload = """
          {
            "orchestration_config": {
              "module_configurations": {
                "llm_module_config": {
                  "model_name":"gpt-4o",
                  "model_params":{
                    "temperature":0.5
                  },
                  "model_version":"latest"
                },
                "templating_module_config":{
                  "template":[
                    {"role":"system","content":"%s"},
                    {"role":"user","content":"%s"}
                  ]
                }
              },
              "stream":false
            },
            "input_params":{},
            "messages_history":[]
          }
          """.formatted(systemMessage.replace("\n"," "), text);
      StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
      httpPost.setEntity(entity);

      return httpClient.execute(httpPost, response ->
          new ObjectMapper()
              .readTree(response.getEntity().getContent())
              .get("orchestration_result")
              .get("choices").get(0)
              .get("message")
              .get("content")
              .asText());
    }
  }

  @GetMapping(value = "/step2", produces = "text/html")
  @Nonnull
  String completionLowLevel(@RequestParam("text") String text) {
    var client = new OrchestrationClient();
    var llmModuleConfig = LLMModuleConfig.create().modelName("gpt-4o").modelParams(Map.of("temperature", 0.5)).modelVersion("latest");
    var system = ChatMessage.create().role("system").content(systemMessage);
    var user = ChatMessage.create().role("user").content(text);
    var moduleConfig = ModuleConfigs.create().llmModuleConfig(llmModuleConfig).templatingModuleConfig(Template.create().template(system, user));
    var postRequest = CompletionPostRequest.create().orchestrationConfig(OrchestrationConfig.create().moduleConfigurations(moduleConfig));

    var result = client.executeRequest(postRequest);
    return ((LLMModuleResultSynchronous) result.getOrchestrationResult()).getChoices().getFirst().getMessage().getContent();
  }

  @GetMapping(value = "/step3", produces = "text/html")
  @Nonnull
  String completionHighLevel(@RequestParam("text") String text) {
    var client = new OrchestrationClient();
    var config = new OrchestrationModuleConfig().withLlmConfig(GPT_4O.withParam(TEMPERATURE, 0.5));
    var prompt = new OrchestrationPrompt(Message.system(systemMessage), Message.user(text));
    var result = client.chatCompletion(prompt, config);
    return result.getContent();
  }
}
