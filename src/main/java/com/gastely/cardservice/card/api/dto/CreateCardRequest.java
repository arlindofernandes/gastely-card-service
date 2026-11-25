package com.gastely.cardservice.card.api.dto;

import java.math.BigDecimal;

import com.gastely.cardservice.card.domain.CardBrand;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCardRequest(
        @Size(max = 100)
        @Pattern(regexp = ".*\\S.*")
        String nickname,

        @NotBlank
        @Size(max = 120)
        String financialInstitution,

        @NotNull
        CardBrand brand,

        @Pattern(regexp = "\\d{4}")
        String lastFourDigits,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal creditLimit,

        @Min(1)
        @Max(31)
        short closingDay,

        @Min(1)
        @Max(31)
        short dueDay,

        boolean favorite
) {
}
