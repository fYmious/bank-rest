package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cards (User)", description = "User operations on their own cards")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Get my cards with optional filter by status")
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
    @Operation(summary = "Get a specific card (must own it)")
    public ResponseEntity<CardResponse> getCard(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            cardService.getCard(id, userDetails.getUsername(), false)
        );
    }

    @PostMapping("/{id}/request-block")
    @Operation(summary = "Request to block own card")
    public ResponseEntity<Void> requestBlock(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        cardService.requestBlock(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between own cards")
    public ResponseEntity<Void> transfer(
        @Valid @RequestBody TransferRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        cardService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
