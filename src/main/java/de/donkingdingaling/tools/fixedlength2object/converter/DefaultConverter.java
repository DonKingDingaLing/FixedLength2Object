package de.donkingdingaling.tools.fixedlength2object.converter;

import de.donkingdingaling.tools.fixedlength2object.exception.ConversionException;

import java.util.Optional;

public class DefaultConverter implements Converter<String> {
    @Override
    public String toJavaType(String textField, String format) throws ConversionException {
        return Optional.ofNullable(textField)
                .orElseThrow(ConversionException::new)
                .trim();
    }

    @Override
    public String fromJavaType(String javaField, String format) throws ConversionException {
        return Optional.ofNullable(javaField)
                .orElseThrow(ConversionException::new)
                .trim();
    }
}
