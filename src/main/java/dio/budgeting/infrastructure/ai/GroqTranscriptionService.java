package dio.budgeting.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class GroqTranscriptionService {

    private final RestClient restClient;
    private final String apiKey;

    public GroqTranscriptionService(@Value("${groq.api-key}") String apiKey) {

        this.apiKey = apiKey;

        this.restClient = RestClient.builder().baseUrl("https://api.groq.com").build();
    }

    public String transcribe(MultipartFile file) throws IOException {

        ByteArrayResource audioResource = new ByteArrayResource(file.getBytes()) {

            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", audioResource);
        body.add("model", "whisper-large-v3-turbo");
        body.add("language", "pt");
        body.add("response_format", "json");
        body.add("temperature", "0");

        Map<?, ?> response = restClient.post().uri("/openai/v1/audio/transcriptions").header("Authorization", "Bearer " + apiKey).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(Map.class);

        if (response == null || response.get("text") == null) {
            throw new RuntimeException("Não foi possível obter a transcrição do áudio.");
        }

        return response.get("text").toString();
    }
}