package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSubscriptionMapperTest {

    CreateSubscriptionMapper createSubscriptionMapper = CreateSubscriptionMapper.getInstance();

    @Test
    void mapSuccess() {
        Subscription expectedResult = Subscription.builder()
                .id(null)
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .build();

        Subscription actualResult = createSubscriptionMapper.map(dto);

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}