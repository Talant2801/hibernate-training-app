package org.nikita.hibernatebookpractice.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nikita.hibernatebookpractice.entity.Price;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

class PriceConverterTest {

    private final PriceConverter converter = new PriceConverter();

    @Test
    void convertsToDatabaseColumn() {
        assertEquals("45.99 USD", converter.convertToDatabaseColumn(new Price(new BigDecimal("45.99"), "USD")));
    }

    @Test
    void convertsToEntityAttribute() {
        assertEquals(new Price(new BigDecimal("45.99"), "USD"), converter.convertToEntityAttribute("45.99 USD"));
    }

    @Test
    void convertsNulls() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"45.99", "", "  ", "45.99 USD extra", "null USD"})
    void rejectsMalformedDatabaseValues(String dbPrice) {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(dbPrice));
    }

    @Test
    void rejectsIncompletePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToDatabaseColumn(new Price(new BigDecimal("9.99"), null)));
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToDatabaseColumn(new Price(null, "USD")));
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToDatabaseColumn(new Price(new BigDecimal("9.99"), "US D")));
    }
}
