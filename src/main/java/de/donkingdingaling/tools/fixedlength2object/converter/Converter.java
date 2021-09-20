package de.donkingdingaling.tools.fixedlength2object.converter;

import de.donkingdingaling.tools.fixedlength2object.exception.ConversionException;

public interface Converter<TYPE> {
    TYPE toJavaType(String textField) throws ConversionException;

    String fromJavaType(TYPE javaField) throws ConversionException;
}
