package com.tdevilleduc.springai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank(message = "Le message ne peut pas être vide.")
    @Size(max = 4000, message = "Le message dépasse la taille maximale autorisée (4000 caractères).")
    String message
) {}
