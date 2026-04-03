package cloud.raina.rainaicodemother.ai;

import cloud.raina.rainaicodemother.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 代码生成服务工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel streamingChatModel;
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.info("AI服务实例删除， appId: {},原因：{}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取服务
     *
     * @param appId 应用 ID
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     *
     * @param appId
     * @return
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("创建新的 AI 服务实例， appId: {}", appId);
        // 根据 appId 创建不同的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库中加载对话历史到内存中
        chatHistoryService.loadAppChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * 创建 AI 代码生成服务（默认提供）
     *
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }
}
