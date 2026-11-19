package ma.ensam.clientpulse.presentation.controller;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dto.BalanceAgeeDto;
import ma.ensam.clientpulse.service.interfaces.BalanceAgeeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/factures/balance-agee")
@RequiredArgsConstructor
public class BalanceAgeeController {

    private final BalanceAgeeService service;

    @GetMapping
    public List<BalanceAgeeDto> balance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.calculer(date != null ? date : LocalDate.now());
    }
}
