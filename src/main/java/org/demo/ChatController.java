package org.demo;

import com.sap.ai.sdk.orchestration.Message;
import com.sap.ai.sdk.orchestration.OrchestrationClient;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

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
  String completion(@RequestParam("text") String text) {
    var prompt = new OrchestrationPrompt(Message.system(systemMessage), Message.user(text));
    var result = client.chatCompletion(prompt, config);
    return result.getContent();
  }
}
