package cloud.raina.rainaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningSteamingChatModelConfig {
    private String baseUrl;
    private String apiKey;

    /**
     * 创建一个流式模型（用于 Vue 项目生成，带工具调用）
     *
     * @return
     */
    @Bean
    public StreamingChatModel reasoningSteamingChatModel() {
        // 测试环境使用
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;
        // 生产环境使用
//        final String modelName = "deepseek-reasoner";
//        final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
