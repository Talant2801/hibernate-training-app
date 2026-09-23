package org.nikita.hibernatebookpractice.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.nikita.hibernatebookpractice.entity.Price;

import java.math.BigDecimal;

@Converter
public class PriceConverter implements AttributeConverter<Price, String> {

    private static final String SEPARATOR = " ";

    @Override
    public String convertToDatabaseColumn(Price price) {
        if (price == null) {
            return null;
        }
        if (price.amount() == null || price.currency() == null) {
            throw new IllegalArgumentException("Price must have both amount and currency, got: " + price);
        }
        if (price.currency().contains(SEPARATOR)) {
            throw new IllegalArgumentException("Currency must not contain a space, got: " + price.currency());
        }
        return price.amount() + SEPARATOR + price.currency();
    }

    @Override
    public Price convertToEntityAttribute(String dbPrice) {
        if (dbPrice == null) {
            return null;
        }

        String[] price = dbPrice.split(SEPARATOR);
        if (price.length != 2) {
            throw new IllegalArgumentException(
                    "Malformed price '" + dbPrice + "', expected '<amount> <currency>'");
        }

        try {
            return new Price(new BigDecimal(price[0]), price[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Malformed price amount '" + price[0] + "' in '" + dbPrice + "'", e);
        }
    }
}
