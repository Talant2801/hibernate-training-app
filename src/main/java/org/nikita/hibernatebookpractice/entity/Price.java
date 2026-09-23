package org.nikita.hibernatebookpractice.entity;

import java.math.BigDecimal;

public record Price(BigDecimal amount, String currency) {
}
