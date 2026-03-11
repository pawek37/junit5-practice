package com.dmdev.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderTest {
    @ParameterizedTest
    @MethodSource("providerNames")
    void findByNameSuccess(String actualResult, Provider expectedResult) {
        Provider find = Provider.findByName(actualResult);
        assertEquals(expectedResult,find);

    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"dummy","111"})
    void findByNameFail(String providerName) {
        assertThrows(NoSuchElementException.class,() -> Provider.findByName(providerName));
    }

    @ParameterizedTest
    @MethodSource("providerNames")
    void findByNameOptSuccess(String providerName, Provider expectedResult) {
        Optional<Provider> actual = Provider.findByNameOpt(providerName);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expectedResult);
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"dummy","111"})
    void findByNameFailOptional(String providerName) {
        Optional<Provider> actual = Provider.findByNameOpt(providerName);

        assertThat(actual).isEmpty();
    }

    public static Stream<Arguments> providerNamesNotExisted() {
        return Stream.of(
                Arguments.of("dummy", null),
                Arguments.of("dummy", null)
        );
    }

    public static Stream<Arguments> providerNames() {
        return Stream.of(
                Arguments.of("apple", Provider.APPLE),
                Arguments.of("google", Provider.GOOGLE)
        );
    }
}