package org.example.resumeai.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置类
 * 配置 LangChain4j 的 ChatMemory 等 Bean
 * ChatModel 由 langchain4j-community-dashscope-spring-boot-starter 自动配置
 */
@Configuration
public class AiConfig {

    /**
     * 配置对话记忆窗口 - 保留最近20条消息
     * 用于保持AI对话上下文的连贯性
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }
}
