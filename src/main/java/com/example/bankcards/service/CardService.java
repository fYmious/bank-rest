package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionUtil encryptionUtil;

    public CardResponse createCard(CreateCardRequest request) {
        User owner = userRepository
            .findById(request.getOwnerId())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "User not found: " + request.getOwnerId()
                )
            );

        String encrypted = encryptionUtil.encrypt(request.getCardNumber());
        Card card = Card.builder()
            .cardNumberEncrypted(encrypted)
            .owner(owner)
            .expiryDate(request.getExpiryDate())
            .status(CardStatus.ACTIVE)
            .balance(request.getInitialBalance())
            .build();
        return toResponse(cardRepository.save(card));
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<CardResponse> getMyCards(
        String username,
        CardStatus status,
        Pageable pageable
    ) {
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (status != null) {
            return cardRepository
                .findByOwnerIdAndStatus(user.getId(), status, pageable)
                .map(this::toResponse);
        }
        return cardRepository
            .findByOwnerId(user.getId(), pageable)
            .map(this::toResponse);
    }

    public CardResponse getCard(Long id, String username, boolean isAdmin) {
        Card card = cardRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Card not found: " + id)
            );
        if (!isAdmin && !card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(card);
    }

    public CardResponse blockCard(Long id) {
        Card card = cardRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Card not found: " + id)
            );
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(cardRepository.save(card));
    }

    public CardResponse activateCard(Long id) {
        Card card = cardRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Card not found: " + id)
            );
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(cardRepository.save(card));
    }

    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Card not found: " + id);
        }
        cardRepository.deleteById(id);
    }

    public void requestBlock(Long id, String username) {
        Card card = cardRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Card not found: " + id)
            );
        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Access denied");
        }
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void transfer(TransferRequest request, String username) {
        Card from = cardRepository
            .findById(request.getFromCardId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Source card not found")
            );
        Card to = cardRepository
            .findById(request.getToCardId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Destination card not found")
            );

        if (!from.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException(
                "Source card does not belong to you"
            );
        }
        if (!to.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException(
                "Destination card does not belong to you"
            );
        }
        if (from.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Source card is not active");
        }
        if (to.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Destination card is not active");
        }
        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));
        cardRepository.save(from);
        cardRepository.save(to);
    }

    public CardResponse toResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        String decrypted = encryptionUtil.decrypt(
            card.getCardNumberEncrypted()
        );
        response.setMaskedNumber(encryptionUtil.mask(decrypted));
        response.setOwnerUsername(card.getOwner().getUsername());
        response.setExpiryDate(card.getExpiryDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        return response;
    }
}
