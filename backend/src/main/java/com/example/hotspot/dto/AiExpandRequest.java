package com.example.hotspot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiExpandRequest {
    @NotBlank
    private String keyword;
}
