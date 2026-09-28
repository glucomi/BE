package com.example.ddadang.domain.member.dto.request;

import com.example.ddadang.domain.member.enums.TermsType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AgreementRequest(
    @NotEmpty @Valid List<Item> agreements
) {

    public record Item(
        @NotNull TermsType termsType,
        @NotNull Boolean agreed
    ) {
    }
}
