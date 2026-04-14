package com.tdevilleduc.springai.validation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PromptValidator {

    private static final int MAX_LENGTH = 1000;

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
        Pattern.compile("(?i)ignore (all |previous |above )?(instructions?|prompts?|rules?)"),
        Pattern.compile("(?i)you are now"),
        Pattern.compile("(?i)act as (a |an )?"),
        Pattern.compile("(?i)system\\s*:"),
        Pattern.compile("(?i)\\[INST\\]"),
        Pattern.compile("(?i)<\\|.*?\\|>")
    );

    public void validate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide.");
        }
        if (input.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Le message dépasse la taille maximale autorisée (%d caractères).".formatted(MAX_LENGTH)
            );
        }
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                throw new IllegalArgumentException("Le message contient du contenu non autorisé.");
            }
        }
    }
}
