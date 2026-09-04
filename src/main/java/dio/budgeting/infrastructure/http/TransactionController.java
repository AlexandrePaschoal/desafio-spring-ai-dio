package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import java.util.UUID;
import dio.budgeting.infrastructure.ai.GroqTranscriptionService;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import dio.budgeting.infrastructure.security.AiRateLimitService;
import dio.budgeting.infrastructure.security.CurrentUserService;

import jakarta.validation.Valid;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private static final int MAX_AI_MESSAGE_LENGTH = 1000;

    private static final long MAX_AUDIO_SIZE =
            10 * 1024 * 1024;

    private static final Set<String> ALLOWED_AUDIO_TYPES =
            Set.of(
                    "audio/webm",
                    "audio/ogg",
                    "audio/mpeg",
                    "audio/mp4",
                    "audio/wav",
                    "audio/x-wav"
            );

    private final PersistTransactionUseCase
            persistTransactionUseCase;

    private final ListTransactionsByCategoryUseCase
            listTransactionsByCategoryUseCase;

    private final ChatClient chatClient;

    private final GroqTranscriptionService
            groqTranscriptionService;

    private final AiRateLimitService
            aiRateLimitService;

    private final CurrentUserService
            currentUserService;

    public TransactionController(
            PersistTransactionUseCase persistTransactionUseCase,
            ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
            @Value("classpath:prompts/system-message.st")
            Resource systemPrompt,
            ChatClient.Builder chatClientBuilder,
            GroqTranscriptionService groqTranscriptionService,
            AiRateLimitService aiRateLimitService,
            CurrentUserService currentUserService
    ) throws IOException {

        this.persistTransactionUseCase =
                persistTransactionUseCase;

        this.listTransactionsByCategoryUseCase =
                listTransactionsByCategoryUseCase;

        this.groqTranscriptionService =
                groqTranscriptionService;

        this.aiRateLimitService =
                aiRateLimitService;

        this.currentUserService =
                currentUserService;

        this.chatClient = chatClientBuilder
                .defaultSystem(
                        systemPrompt.getContentAsString(
                                Charset.defaultCharset()
                        )
                )
                .defaultTools(
                        persistTransactionUseCase,
                        listTransactionsByCategoryUseCase
                )
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @Valid
            @RequestBody
            TransactionRequest request
    ) {

        var transaction =
                persistTransactionUseCase.execute(
                        request.toInput()
                );

        return TransactionResponse.from(transaction);
    }

    @GetMapping("/{categoryId}")
    public List<TransactionResponse> readTransactions(
            @PathVariable UUID categoryId
    ) {

        return listTransactionsByCategoryUseCase
                .execute(categoryId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @PostMapping(
            value = "/ai",
            consumes = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> processWithAi(
            @RequestBody String userMessage
    ) {

        if (userMessage == null
                || userMessage.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "A mensagem não pode estar vazia."
                    );
        }

        String normalizedMessage =
                userMessage.trim();

        if (normalizedMessage.length()
                > MAX_AI_MESSAGE_LENGTH) {

            return ResponseEntity
                    .status(
                            HttpStatus.PAYLOAD_TOO_LARGE
                    )
                    .body(
                            "A mensagem excede o limite de 1000 caracteres."
                    );
        }

        ResponseEntity<String> rateLimitResponse =
                validateAiRateLimit();

        if (rateLimitResponse != null) {
            return rateLimitResponse;
        }

        String response = chatClient
                .prompt()
                .user(normalizedMessage)
                .call()
                .content();

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/ai/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> processAudioWithAi(
            @RequestParam("file")
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "O arquivo de áudio não pode estar vazio."
                    );
        }

        if (file.getSize() > MAX_AUDIO_SIZE) {

            return ResponseEntity
                    .status(
                            HttpStatus.PAYLOAD_TOO_LARGE
                    )
                    .body(
                            "O arquivo de áudio excede o limite de 10 MB."
                    );
        }

        String contentType =
                file.getContentType();

        if (!isAllowedAudioType(contentType)) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNSUPPORTED_MEDIA_TYPE
                    )
                    .body(
                            "Tipo de arquivo de áudio não suportado."
                    );
        }

        ResponseEntity<String> rateLimitResponse =
                validateAiRateLimit();

        if (rateLimitResponse != null) {
            return rateLimitResponse;
        }

        try {

            String transcription =
                    groqTranscriptionService
                            .transcribe(file);

            String response = chatClient
                    .prompt()
                    .user(transcription)
                    .call()
                    .content();

            return ResponseEntity.ok(response);

        } catch (IOException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Erro ao processar o arquivo de áudio."
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Erro ao processar a solicitação com IA."
                    );
        }
    }

    private ResponseEntity<String>
    validateAiRateLimit() {

        var userId =
                currentUserService.getCurrentUserId();

        boolean allowed =
                aiRateLimitService
                        .tryConsume(userId);

        if (!allowed) {

            return ResponseEntity
                    .status(
                            HttpStatus.TOO_MANY_REQUESTS
                    )
                    .body(
                            "Limite de solicitações de IA excedido. Tente novamente em alguns instantes."
                    );
        }

        return null;
    }

    private boolean isAllowedAudioType(
            String contentType
    ) {

        if (contentType == null
                || contentType.isBlank()) {

            return false;
        }

        String normalizedContentType =
                contentType
                        .toLowerCase()
                        .split(";")[0]
                        .trim();

        return ALLOWED_AUDIO_TYPES
                .contains(
                        normalizedContentType
                );
    }
}