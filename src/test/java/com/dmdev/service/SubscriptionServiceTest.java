package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.ValidationResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {


    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private Clock clock;
    @InjectMocks
    private SubscriptionService service;
    private CreateSubscriptionDto dto;
    private Subscription existingSub;
    private Subscription newSub;

    @BeforeEach
    void setUp() {
        dto = getCreateSubscriptionDto();
        existingSub = getCreateExistingSubscription();
        newSub = getCreateNEWSubscription();
    }

    @Test
    void upsertSubscriptionStatusExpiredFoundInDB() {
        Subscription existingExpiredSub = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Spotify")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2025-10-05T14:30:00Z"))
                .status(Status.EXPIRED)
                .build();
        List<Subscription> subscriptionList = Arrays.asList(existingExpiredSub, existingSub);

        ValidationResult validationResult = new ValidationResult();
        doReturn(validationResult).when(createSubscriptionValidator).validate(dto);
        doReturn(subscriptionList).when(subscriptionDao).findByUserId(dto.getUserId());
        doReturn(existingExpiredSub).when(subscriptionDao).upsert(any());
        Subscription actualResult = service.upsert(dto);

        assertAll(
                () -> assertEquals(Status.ACTIVE, actualResult.getStatus()),
                () -> assertEquals(dto.getExpirationDate(), actualResult.getExpirationDate()),
                () -> Mockito.verify(subscriptionDao).upsert(any()),
                () -> Mockito.verifyNoInteractions(createSubscriptionMapper)
        );
    }

    @Test
    void upsertSubscriptionStatusActiveFoundInDB() {
        Subscription existingActiveSub = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Spotify")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2027-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();
        List<Subscription> subscriptionList = Arrays.asList(existingActiveSub, existingSub);

        ValidationResult validationResult = new ValidationResult();
        doReturn(validationResult).when(createSubscriptionValidator).validate(dto);
        doReturn(subscriptionList).when(subscriptionDao).findByUserId(dto.getUserId());
        doReturn(existingActiveSub).when(subscriptionDao).upsert(any());

        Subscription actualResult = service.upsert(dto);

        assertAll(
                () -> assertEquals(Status.ACTIVE, actualResult.getStatus()),
                () -> assertEquals(dto.getExpirationDate(), actualResult.getExpirationDate()),
                () -> Mockito.verify(subscriptionDao).upsert(any()),
                () -> Mockito.verifyNoInteractions(createSubscriptionMapper)
        );
    }

    @Test
    void upsertSubscriptionNotFoundInDB() {
        Subscription newnewSub = Subscription.builder()
                .id(2)
                .userId(1)
                .name("Spotify")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2030-10-05T14:30:00Z"))
                .status(Status.ACTIVE).build();

        List<Subscription> subscriptionEmptyList = new ArrayList<>();

        ValidationResult validationResult = new ValidationResult();
        doReturn(validationResult).when(createSubscriptionValidator).validate(dto);
        doReturn(subscriptionEmptyList).when(subscriptionDao).findByUserId(dto.getUserId());
        doReturn(newnewSub).when(createSubscriptionMapper).map(dto);
        doReturn(newnewSub).when(subscriptionDao).upsert(newnewSub);

        Subscription actualResult = service.upsert(dto);

        assertAll(
                () -> Mockito.verify(createSubscriptionMapper).map(dto),
                () -> Mockito.verify(subscriptionDao).upsert(newnewSub),
                () -> Mockito.verify(subscriptionDao).findByUserId(dto.getUserId()),
                () -> Assertions.assertThat(actualResult).isEqualTo(newnewSub)
        );
    }

    @Test
    void cancelSubscriptionNotFoundToBeCanceledThrowsException() {

        doReturn(Optional.empty()).when(subscriptionDao).findById(0);
        assertThrows(IllegalArgumentException.class, () -> service.cancel(0));
        Mockito.verify(subscriptionDao).findById(any());
        Mockito.verify(subscriptionDao, Mockito.never()).update(any());

    }

    @Test
    void cancelSubscriptionAlreadyExpiredThrowsException() {
        Subscription subscriptionExpired = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2025-10-05T14:30:00Z"))
                .status(Status.EXPIRED)
                .build();

        doReturn(Optional.of(subscriptionExpired)).when(subscriptionDao).findById(subscriptionExpired.getId());

        assertThrows(SubscriptionException.class, () -> service.cancel(subscriptionExpired.getId()));
        Mockito.verify(subscriptionDao, Mockito.never()).update(any());
    }

    @Test
    void cancelSubscriptionSuccessful() {
        Subscription activeSubscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2028-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();

        doReturn(Optional.of(activeSubscription)).when(subscriptionDao).findById(activeSubscription.getId());

        service.cancel(activeSubscription.getId());

        assertAll(
                () -> assertEquals(Status.CANCELED, activeSubscription.getStatus()),
                () -> Mockito.verify(subscriptionDao).update(any())
        );
    }

    @Test
    void expireShouldThrowExceptionWhenSubscriptionNotFoundByID() {
        assertThrows(IllegalArgumentException.class, () -> service.expire(0));
    }

    @Test
    void expireShouldThrowExceptionWhenSubscriptionAlreadyExpired() {
        Subscription activeSubscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2028-10-05T14:30:00Z"))
                .status(Status.EXPIRED)
                .build();

        doReturn(Optional.of(activeSubscription)).when(subscriptionDao).findById(activeSubscription.getId());

        assertThrows(SubscriptionException.class, () -> service.expire(activeSubscription.getId()));
    }

    @Test
    void expireSuccess() {
        Instant fixedInstant = Instant.parse("2030-01-01T10:00:00Z");
        Subscription activeSubscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2028-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();

        doReturn(Optional.of(activeSubscription)).when(subscriptionDao).findById(activeSubscription.getId());
        doReturn(fixedInstant).when(clock).instant();

        service.expire(activeSubscription.getId());

        assertAll(
                () -> Assertions.assertThat(activeSubscription.getStatus()).isEqualTo(Status.EXPIRED),
                () -> Assertions.assertThat(activeSubscription.getExpirationDate()).isEqualTo(fixedInstant),
                () -> Mockito.verify(subscriptionDao).update(activeSubscription)
        );

    }

    private static CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Spotify")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2035-10-05T14:30:00Z"))
                .build();
    }

    private static Subscription getCreateExistingSubscription() {
        return Subscription.builder()
                .id(1)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();
    }

    private static Subscription getCreateNEWSubscription() {
        return Subscription.builder()
                .id(2)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2030-10-05T14:30:00Z"))
                .status(Status.ACTIVE).build();
    }
}
