package budget;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// Asks the AI model for budget advice
@Service
public class AiService {

    @Value("${ai.api.key}") // the API key comes from application.properties
    private String apiKey;

    private final RestClient client = RestClient.create("https://api.anthropic.com");

    public List<String> advice(double income, double estimate, Map<String, Double> perCategory) {
        String question = "I earn " + income + " kr this month. "
                + "My expected spending next month is " + estimate + " kr. "
                + "My spending per category is " + perCategory + ". "
                + "Give me 3 short, concrete tips to improve my budget. "
                + "Write one tip per line, max 20 words each, no numbering.";

        // The message I send, in the format the Anthropic API wants
        Map<String, Object> body = Map.of(
                "model", "claude-haiku-5-5",
                "max_tokens", 1500,
                "messages", List.of(Map.of("role", "user", "content", question)));

        JsonNode response = client.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        // The answer can have several parts, I collect all the text parts
        String answer = "";
        for (JsonNode part : response.path("content")) {
            answer += part.path("text").asText();
        }
        if (answer.isBlank()) {
            System.out.println("AI gave no text: " + response); // shows the full answer in the console
        }

        // one tip per line
        return answer.lines().filter(line -> !line.isBlank()).toList();
    }
}
