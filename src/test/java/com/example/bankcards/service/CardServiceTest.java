package com.example.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardEncryptionUtil encryptionUtil;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card activeCard;
    private Card blockedCard;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .username("testuser")
            .role(Role.ROLE_USER)
            .build();
        activeCard = Card.builder()
            .id(1L)
            .owner(user)
            .status(CardStatus.ACTIVE)
            .balance(new BigDecimal("1000"))
            .cardNumberEncrypted("enc1")
            .expiryDate(LocalDate.now().plusYears(2))
            .build();
        blockedCard = Card.builder()
            .id(2L)
            .owner(user)
            .status(CardStatus.BLOCKED)
            .balance(new BigDecimal("500"))
            .cardNumberEncrypted("enc2")
            .expiryDate(LocalDate.now().plusYears(2))
            .build();
    }

    @Test
    void createCard_success() {
        CreateCardRequest req = new CreateCardRequest();
        req.setCardNumber("1234567890123456");
        req.setOwnerId(1L);
        req.setExpiryDate(LocalDate.now().plusYears(2));
        req.setInitialBalance(BigDecimal.valueOf(1000));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn(
            "encrypted"
        );
        when(cardRepository.save(any())).thenReturn(activeCard);
        when(encryptionUtil.decrypt("enc1")).thenReturn("1234567890123456");
        when(encryptionUtil.mask("1234567890123456")).thenReturn(
            "**** **** **** 3456"
        );

        var response = cardService.createCard(req);
        assertThat(response).isNotNull();
        verify(cardRepository, times(1)).save(any());
    }

    @Test
    void transfer_insufficientBalance_throwsBadRequest() {
        TransferRequest req = new TransferRequest();
        req.setFromCardId(1L);
        req.setToCardId(2L);
        req.setAmount(new BigDecimal("5000"));

        Card toCard = Card.builder()
            .id(2L)
            .owner(user)
            .status(CardStatus.ACTIVE)
            .balance(BigDecimal.ZERO)
            .cardNumberEncrypted("enc2")
            .expiryDate(LocalDate.now().plusYears(2))
            .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transfer(req, "testuser"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Insufficient balance");
    }

    @Test
    void transfer_fromBlockedCard_throwsBadRequest() {
        TransferRequest req = new TransferRequest();
        req.setFromCardId(2L);
        req.setToCardId(1L);
        req.setAmount(new BigDecimal("100"));

        when(cardRepository.findById(2L)).thenReturn(Optional.of(blockedCard));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));

        assertThatThrownBy(() -> cardService.transfer(req, "testuser"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("not active");
    }

    @Test
    void blockCard_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardRepository.save(any())).thenReturn(activeCard);
        when(encryptionUtil.decrypt("enc1")).thenReturn("1234567890123456");
        when(encryptionUtil.mask("1234567890123456")).thenReturn(
            "**** **** **** 3456"
        );

        var response = cardService.blockCard(1L);
        assertThat(response).isNotNull();
        verify(cardRepository).save(any());
    }
}
