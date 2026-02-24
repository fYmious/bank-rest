package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) CardStatus status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            cardService.getMyCards(userDetails.getUsername(), status, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCard(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            cardService.getCard(id, userDetails.getUsername(), false)
        );
    }

    @PostMapping("/{id}/request-block")
    public ResponseEntity<Void> requestBlock(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        cardService.requestBlock(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
        @Valid @RequestBody TransferRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        cardService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
