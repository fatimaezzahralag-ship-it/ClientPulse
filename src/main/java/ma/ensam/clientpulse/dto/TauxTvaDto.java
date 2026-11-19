package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Un taux de TVA. taux est en pourcentage : 20 veut dire 20 %. */
public record TauxTvaDto(Long id,
                         @NotBlank String libelle,
                         @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taux,
                         boolean actif) {
}
