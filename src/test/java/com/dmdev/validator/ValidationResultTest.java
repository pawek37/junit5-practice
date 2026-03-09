package com.dmdev.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {
    private final List<Error> errors = new ArrayList<>();

    @Test
    void addElementShouldChangeSizeOfErrorList() {
//        errors.add(Error.of(100, "userId is invalid"));
//        assertThat(errors).hasSize(1);
    }

    @Test
    void hasErrors() {
//        errors.add(Error.of(101, "name is invalid"));
//        errors.add(Error.of(102, "name is invalid"));
//        assertThat(errors).isNotEmpty();
    }

    @Test
    void getErrors() {

    }
}