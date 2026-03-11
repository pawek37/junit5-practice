package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void validationSuccess() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);
        assertFalse(actualResult.hasErrors());
    }
    @Test
    void validationFailByName() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);
        assertThat(actualResult.getErrors().get(0)).isEqualTo(Error.of(101,"name is invalid"));
    }
    @Test
    void validationFailByUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0)).isEqualTo(Error.of(100,"userId is invalid"));
    }
    @Test
    void validationFailByNullProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(0)
                .name("Ivan")
                .provider(null)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0)).isEqualTo(Error.of(102,"provider is invalid"));
    }
    @Test
    void validationFailByWrongProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(0)
                .name("Ivan")
                .provider("Yandex")
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0)).isEqualTo(Error.of(102,"provider is invalid"));
    }
    @Test
    void validationFailByWrongExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(0)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.parse("2021-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0)).isEqualTo(Error.of(103,"expirationDate is invalid"));
    }
    @Test
    void validationFailByNullExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(0)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(null)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0)).isEqualTo(Error.of(103,"expirationDate is invalid"));
    }
    @Test
    void validationFailByWrongDifferentValues() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("")
                .provider(null)
                .expirationDate(Instant.parse("2021-10-05T14:30:00Z"))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(4);

        List<Integer> list = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();
        assertThat(actualResult.getErrors()).contains(Error.of(100,"userId is invalid"),
                Error.of(101,"name is invalid"),
                Error.of(102,"provider is invalid"),
                Error.of(103,"expirationDate is invalid"));
    }
}