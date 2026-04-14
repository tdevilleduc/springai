package com.tdevilleduc.springai;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class SpringaiApplicationTests {

    @MockitoBean
    AnthropicChatModel chatModel;

    @Test
    void contextLoads() {
    }

    @Test
    void main_shouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            SpringaiApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(SpringaiApplication.class, new String[]{}));
        }
    }

}
