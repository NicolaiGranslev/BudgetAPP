package dk.budget;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Sends a receipt image to the OCR.space API and gets the text back
@Service
public class OcrService {

    @Value("${ocr.api.key}")  // the API key comes from application.properties
    private String apiKey;

    private final RestClient client = RestClient.create("https://api.ocr.space");

    public String readText(MultipartFile image) throws IOException {
        // the image is wrapped so it is sent as a file upload
        ByteArrayResource file = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() { return image.getOriginalFilename(); }
        };

        LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("apikey", apiKey);
        form.add("language", "dan");
        form.add("file", file);

        JsonNode response = client.post()
                .uri("/parse/image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        return response.path("ParsedResults").path(0).path("ParsedText").asText("");
    }

    // Finds the total on the receipt, e.g. "TOTAL 245,50" or "I alt 99,95"
    public double findTotal(String text) {
        Pattern pattern = Pattern.compile("(?i)(total|i alt|at betale|sum)[^0-9]*([0-9]+[.,][0-9]{2})");
        Matcher matcher = pattern.matcher(text);
        double total = 0;
        while (matcher.find()) {
            total = Double.parseDouble(matcher.group(2).replace(",", "."));
        }
        return total;
    }
}
