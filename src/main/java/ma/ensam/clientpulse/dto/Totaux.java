package ma.ensam.clientpulse.dto;
import java.math.BigDecimal;
import java.util.Map;

public record Totaux(BigDecimal totalHT , BigDecimal totalTVA , BigDecimal totalTTC , Map<BigDecimal, BigDecimal> tvaParTaux){}