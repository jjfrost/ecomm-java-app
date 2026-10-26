package com.ecommerce.common.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OracleYesNoConverterTest {
    private final OracleYesNoConverter converter = new OracleYesNoConverter();

    @Test
    void convertsBooleansToOracleFlags() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("Y");
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertsOracleFlagsToBooleans() {
        assertThat(converter.convertToEntityAttribute("Y")).isTrue();
        assertThat(converter.convertToEntityAttribute("y")).isTrue();
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
