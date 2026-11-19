package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.TrancheRetard;
import ma.ensam.clientpulse.dto.BalanceAgeeDto;
import ma.ensam.clientpulse.service.interfaces.BalanceAgeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class BalanceAgeeServiceImpl implements BalanceAgeeService {

    private final FactureDao factureDao;

    @Override
    @Transactional(readOnly = true)
    public List<BalanceAgeeDto> calculer(LocalDate jour) {
        Map<Long, Map<TrancheRetard, BigDecimal>> parClient = new TreeMap<>();

        for (Facture facture : factureDao.findOuvertes()) {
            BigDecimal reste = facture.getResteAPayer();
            if (reste.signum() <= 0) {
                continue;
            }
            long retard = facture.getDateEcheance() == null
                    ? 0 : ChronoUnit.DAYS.between(facture.getDateEcheance(), jour);
            TrancheRetard tranche = TrancheRetard.pour(retard);

            Map<TrancheRetard, BigDecimal> tranches = parClient
                    .computeIfAbsent(facture.getClient().getId(), id -> new EnumMap<>(TrancheRetard.class));
            tranches.merge(tranche, reste, BigDecimal::add);
        }

        List<BalanceAgeeDto> resultat = new ArrayList<>();
        for (Map.Entry<Long, Map<TrancheRetard, BigDecimal>> e : parClient.entrySet()) {
            Map<TrancheRetard, BigDecimal> t = e.getValue();
            resultat.add(new BalanceAgeeDto(e.getKey(),
                    montant(t, TrancheRetard.NON_ECHU),
                    montant(t, TrancheRetard.DE_1_A_30),
                    montant(t, TrancheRetard.DE_31_A_60),
                    montant(t, TrancheRetard.DE_61_A_90),
                    montant(t, TrancheRetard.PLUS_DE_90)));
        }
        return resultat;
    }

    private BigDecimal montant(Map<TrancheRetard, BigDecimal> tranches, TrancheRetard tranche) {
        return tranches.getOrDefault(tranche, BigDecimal.ZERO);
    }
}
