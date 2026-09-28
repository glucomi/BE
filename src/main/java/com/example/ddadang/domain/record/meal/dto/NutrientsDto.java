package com.example.ddadang.domain.record.meal.dto;

import com.example.ddadang.domain.record.meal.entity.Nutrients;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "영양 성분. 열량 외 항목은 없으면 null")
public record NutrientsDto(
    @Schema(description = "열량(kcal)") @NotNull @DecimalMin("0") BigDecimal kcal,
    @Schema(description = "탄수화물(g)") @DecimalMin("0") BigDecimal carbohydrateG,
    @Schema(description = "당류(g)") @DecimalMin("0") BigDecimal sugarsG,
    @Schema(description = "식이섬유(g)") @DecimalMin("0") BigDecimal dietaryFiberG,
    @Schema(description = "단백질(g)") @DecimalMin("0") BigDecimal proteinG,
    @Schema(description = "지방(g)") @DecimalMin("0") BigDecimal fatG,
    @Schema(description = "포화지방(g)") @DecimalMin("0") BigDecimal saturatedFatG,
    @Schema(description = "트랜스지방(g)") @DecimalMin("0") BigDecimal transFatG,
    @Schema(description = "지방산(g)") @DecimalMin("0") BigDecimal fattyAcidG,
    @Schema(description = "불포화지방산(g)") @DecimalMin("0") BigDecimal unsaturatedFatG,
    @Schema(description = "콜레스테롤(mg)") @DecimalMin("0") BigDecimal cholesterolMg,
    @Schema(description = "나트륨(mg)") @DecimalMin("0") BigDecimal sodiumMg,
    @Schema(description = "카페인(mg)") @DecimalMin("0") BigDecimal caffeineMg
) {

    public static NutrientsDto from(Nutrients n) {
        return new NutrientsDto(
            n.getKcal(), n.getCarbohydrateG(), n.getSugarsG(), n.getDietaryFiberG(), n.getProteinG(), n.getFatG(),
            n.getSaturatedFatG(), n.getTransFatG(), n.getFattyAcidG(), n.getUnsaturatedFatG(),
            n.getCholesterolMg(), n.getSodiumMg(), n.getCaffeineMg()
        );
    }

    public Nutrients toEntity() {
        return new Nutrients(
            kcal, carbohydrateG, sugarsG, dietaryFiberG, proteinG, fatG, saturatedFatG, transFatG,
            fattyAcidG, unsaturatedFatG, cholesterolMg, sodiumMg, caffeineMg
        );
    }
}
