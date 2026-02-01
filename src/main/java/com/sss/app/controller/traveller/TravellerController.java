package com.sss.app.controller.traveller;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import com.sss.app.service.lead.LeadService;
import com.sss.app.service.traveller.TravellerService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/traveller")
public class TravellerController {
    private final TravellerService travellerService;

    public TravellerController(TravellerService travellerService) {
        this.travellerService = travellerService;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TravellerResponseDTO> createTraveller(@Valid @RequestBody TravellerCreateRequestDTO payload) {
        System.out.println("CONTROLLER firstName = " + payload.getFirstName());
        return ResponseEntity.ok(travellerService.createTraveller(payload));
    }

    @PutMapping(value = "/{id}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TravellerResponseDTO> updateTraveller(
            @PathVariable Long id,
            @Valid @RequestBody TravellerUpdateRequestDTO payload) {

        return ResponseEntity.ok(travellerService.updateTraveller(id, payload));
    }
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TravellerResponseDTO> getTravellerById(@PathVariable Long id) {
        return ResponseEntity.ok(travellerService.getTravellerById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraveller(@PathVariable Long id) {
        travellerService.deleteTraveller(id);
        return ResponseEntity.noContent().build();
    }
  /*  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LeadResponseDTO>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }*/

}
