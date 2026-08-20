package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.ai.GroqTranscriptionService;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final ChatClient chatClient;
    private final GroqTranscriptionService groqTranscriptionService;

    public TransactionController(PersistTransactionUseCase persistTransactionUseCase, ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase, @Value("classpath:prompts/system-message.st") Resource systemPrompt, ChatClient.Builder chatClientBuilder, GroqTranscriptionService groqTranscriptionService) throws IOException {

        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.groqTranscriptionService = groqTranscriptionService;

        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt.getContentAsString(Charset.defaultCharset())).defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase).build();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {

        var transaction = persistTransactionUseCase.execute(request.toInput());

        return TransactionResponse.from(transaction);
    }


    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category) {

        return listTransactionsByCategoryUseCase.execute(category).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping(value = "/ai", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String processWithAi(@RequestBody String userMessage) {

        return chatClient.prompt().user(userMessage).call().content();
    }

    @PostMapping(value = "/ai/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> processAudioWithAi(@RequestParam("file") MultipartFile file) {


        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("O arquivo de áudio não pode estar vazio.");
        }

        try {

            String transcription = groqTranscriptionService.transcribe(file);


            String response = chatClient.prompt().user(transcription).call().content();

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao processar o arquivo de áudio.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar a solicitação com IA.");
        }
    }
}