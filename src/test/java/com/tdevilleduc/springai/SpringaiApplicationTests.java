package com.tdevilleduc.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SpringaiApplicationTests {

    @MockitoBean
    AnthropicChatModel chatModel;

    @Test
    void contextLoads() {
    }

}
