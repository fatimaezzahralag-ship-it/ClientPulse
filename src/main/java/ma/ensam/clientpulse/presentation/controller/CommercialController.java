package ma.ensam.clientpulse.presentation.controller;

import ma.ensam.clientpulse.dto.CommercialDto;
import ma.ensam.clientpulse.service.interfaces.CommercialService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/commerciaux")
public class CommercialController {

    private final CommercialService commercialService;

    public CommercialController(CommercialService commercialService) {
        this.commercialService = commercialService;
    }

    @GetMapping
    public ResponseEntity<List<CommercialDto>> findAll() {
        return ResponseEntity.ok(commercialService.findAll());
    }
}
