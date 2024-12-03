package org.demo;

import com.sap.ai.sdk.orchestration.Message;
import com.sap.ai.sdk.orchestration.OrchestrationClient;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import org.springframework.beans.factory.annotation.Autowired;
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

  @GetMapping(value = "/step1", produces = "text/html")
  @Nonnull
  String completion(@RequestParam("text") String text) {
    var prompt = new OrchestrationPrompt(
        Message.system("Render your response with HTML, use <p> tag to separate sentences. Use <b>, <i> and <u> to emphasize words. Try to imitate a German accent throughout the response. Limit your response to 5 sentences."),
        Message.user(text));
    var result = client.chatCompletion(prompt, config);
    return result.getContent();
  }
}
