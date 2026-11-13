package budget;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Sends a receipt picture to the OCR.space API and reads the text on it
@Service
public class OcrService {

    @Value("${ocr.api.key}") // the API key comes from application.properties
    private String apiKey;

    private final RestClient client = RestClient.create("https://api.ocr.space");

    // "TOTAL 321,00" -> 321,00
    private static final Pattern TOTAL = Pattern.compile("(?i)(total|i alt|at betale)\\D*(\\d+[.,]\\d{2})");
    // "11-11-2026", "11.11.26" or "11 11 26"
    private static final Pattern DATE = Pattern.compile("(\\d{1,2})[-./\\s]+(\\d{1,2})[-./\\s]+(\\d{2,4})");

    // Sends the picture and our API key, gets the text back
    public String readText(MultipartFile image) throws IOException {
        ByteArrayResource file = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() { return "receipt.jpg"; }
        };

        LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("apikey", apiKey);
        form.add("OCREngine", "2"); // I use engine 2 because its best at receipts
        form.add("file", file);

        JsonNode response = client.post()
                .uri("/parse/image")
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        return response.path("ParsedResults").path(0).path("ParsedText").asText("");
    }

    // Finds the amount after the word TOTAL
    public double findTotal(String text) {
        Matcher m = TOTAL.matcher(text);
        if (m.find()) {
            return Double.parseDouble(m.group(2).replace(",", "."));
        }
        return 0;
    }

    // Finds the first real date on the receipt
    public LocalDate findDate(String text) {
        Matcher m = DATE.matcher(text);
        while (m.find()) {
            try {
                int day = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int year = Integer.parseInt(m.group(3));
                if (year < 100) year += 2000; // "26" -> 2026

                LocalDate date = LocalDate.of(year, month, day);
                if (year >= 2020 && !date.isAfter(LocalDate.now())) {
                    return date;
                }
            } catch (Exception e) {
                // not a real date,  ex "31 1 428" -> try the next one
            }
        }
        return null;
    }
}
