package com.dmdev.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesUtilTest {


    @ParameterizedTest
    @MethodSource("getParamsForPropertiesUtilMethod")
    void get(String key, String expectedValue) {
        String actualResult = PropertiesUtil.get(key);

        assertThat(actualResult).isEqualTo(expectedValue);
    }

    public static Stream<Arguments> getParamsForPropertiesUtilMethod() {
        return Stream.of(
                Arguments.of("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
                Arguments.of("db.user", "sa"),
                Arguments.of("db.password", ""),
                Arguments.of("db.driver", "org.h2.Driver")
        );
    }
}