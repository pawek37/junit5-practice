package com.dmdev.integration;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.service.SubscriptionService;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionServiceIT extends IntegrationTestBase {
    private SubscriptionService service;
    private SubscriptionDao subscriptionDao;
    private Clock clock;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        service = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                clock = Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"),
                        ZoneOffset.UTC)
        );
    }

    @Test
    void upsert() {
        CreateSubscriptionDto dto = getCreateSubscriptionDto();

        Subscription upsert = service.upsert(dto);

        Subscription actualResult = subscriptionDao.findById(upsert.getId()).orElseThrow();

        assertThat(actualResult.getUserId()).isEqualTo(dto.getUserId());
        assertThat(actualResult.getName()).isEqualTo(dto.getName());
        assertThat(actualResult.getProvider()).isEqualTo(Provider.APPLE);
        assertThat(actualResult.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void cancel() {

        Subscription saved = service.upsert(getCreateSubscriptionDto());
        service.cancel(saved.getId());
        Optional<Subscription> actualResult = subscriptionDao.findById(saved.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getStatus()).isEqualTo(Status.CANCELED);
        assertThat(actualResult.get().getName()).isEqualTo(getCreateSubscriptionDto().getName());
        assertThat(actualResult.get().getProvider().name()).isEqualTo(getCreateSubscriptionDto().getProvider());
        assertThat(actualResult.get().getExpirationDate()).isEqualTo(getCreateSubscriptionDto().getExpirationDate());
    }

    @Test
    void expire() {
        CreateSubscriptionDto dto = getCreateSubscriptionDto();

        Subscription saved = service.upsert(dto);
        service.expire(saved.getId());
        Optional<Subscription> actualResult = subscriptionDao.findById(saved.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(actualResult.get().getExpirationDate()).isEqualTo(clock.instant());
    }

    private static CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Spotify")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2035-10-05T14:30:00Z"))
                .build();
    }
}
