package com.tdevilleduc.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableAutoConfiguration(exclude = AnthropicChatAutoConfiguration.class)
class SpringaiApplicationTests {

    @MockitoBean
    AnthropicChatModel chatModel;

    @Test
    void contextLoads() {
    }

}
