package org.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.ai.sdk.orchestration.Message;
import com.sap.ai.sdk.orchestration.OrchestrationClient;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.io.IOException;

@RestController
@RequestMapping("/chat")
public class ChatController {

  @Autowired
  OrchestrationClient client;

  @Autowired
  OrchestrationModuleConfig config;

  @Value("${chat.system.message}")
  String systemMessage;

  @GetMapping(value = "/step1", produces = "text/html")
  @Nonnull
  String completionVanilla(@RequestParam("text") String text) throws IOException {
    String HOST =  null;
    String DEPLOYMENT = null;
    String RESOURCE_GROUP = null;
    String AUTH_TOKEN = null;

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
                    {"role":"system","content":"Render your response with HTML, use <p> tag to separate sentences.\\nUse <b>, <i> and <u> to emphasize words.\\nTry to imitate a German accent throughout the response.\\nLimit your response to 5 sentences."},
                    {"role":"user","content":"What's the difference between Java and JavaScript?"}
                  ]
                }
              },
              "stream":false
            },
            "input_params":{},
            "messages_history":[]
          }
          """;
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
    var prompt = new OrchestrationPrompt(Message.system(systemMessage), Message.user(text));
    var result = client.chatCompletion(prompt, config);
    return result.getContent();
  }

  @GetMapping(value = "/step3", produces = "text/html")
  @Nonnull
  String completionHighLevel(@RequestParam("text") String text) {
    var prompt = new OrchestrationPrompt(Message.system(systemMessage), Message.user(text));
    var result = client.chatCompletion(prompt, config);
    return result.getContent();
  }
}
