package de.donkingdingaling.tools.fixedlength2object.converter;

import de.donkingdingaling.tools.fixedlength2object.exception.ConversionException;

public interface Converter<TYPE> {
    TYPE toJavaType(String textField, String format) throws ConversionException;

    String fromJavaType(TYPE javaField, String format) throws ConversionException;
}
