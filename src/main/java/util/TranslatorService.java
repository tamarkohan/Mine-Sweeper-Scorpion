package util;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.io.InputStream;
import java.util.Properties;

public class TranslatorService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final String endpoint;
    private final String key;
    private final String region; // can be empty

    public TranslatorService(String endpoint, String key, String region) {
        this.endpoint = endpoint;
        this.key = key;
        this.region = region;
    }

    public static TranslatorService fromEnvOrResource() {
        // 1) Try env vars (optional)
        String key = System.getenv("AZ_TRANSLATOR_KEY");
        String region = System.getenv("AZ_TRANSLATOR_REGION");
        String endpoint = System.getenv("AZ_TRANSLATOR_ENDPOINT");

        // 2) If missing, try translator.properties packaged inside the JAR
        if (key == null || key.isBlank()) {
            try (InputStream in = TranslatorService.class.getClassLoader()
                    .getResourceAsStream("translator.properties")) {

                if (in != null) {
                    Properties p = new Properties();
                    p.load(in);
                    key = p.getProperty("key", "");
                    region = p.getProperty("region", "");
                    endpoint = p.getProperty("endpoint", "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "https://api.cognitive.microsofttranslator.com";
        }
        if (region == null) region = "";

        if (key == null || key.isBlank()) {
            throw new RuntimeException("Missing translator key (env vars and translator.properties not found)");
        }

        return new TranslatorService(endpoint, key, region);
    }


    public String translate(String text, String from, String to) throws Exception {
        if (text == null || text.isBlank()) return text;

        String url = endpoint + "/translate?api-version=3.0&from=" + from + "&to=" + to;

        String safe = text.replace("\\", "\\\\").replace("\"", "\\\"");
        String body = "[{\"Text\":\"" + safe + "\"}]";

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Ocp-Apim-Subscription-Key", key)
                .header("X-ClientTraceId", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        if (!region.isBlank()) {
            req.header("Ocp-Apim-Subscription-Region", region);
        }

        HttpResponse<String> resp = http.send(req.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Translate failed: " + resp.statusCode() + " " + resp.body());
        }

        // Response: [ { "translations": [ { "text": "....", "to": "en" } ] } ]
        return extractFirstTranslatedText(resp.body());
    }

    private static String extractFirstTranslatedText(String json) {
        String marker = "\"text\":\"";
        int i = json.indexOf(marker);
        if (i < 0) return json;
        i += marker.length();
        int j = json.indexOf("\"", i);
        if (j < 0) return json;
        String s = json.substring(i, j);
        return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
