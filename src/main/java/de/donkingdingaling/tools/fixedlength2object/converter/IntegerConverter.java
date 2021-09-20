package de.donkingdingaling.tools.fixedlength2object.converter;

import java.util.Optional;

public class IntegerConverter implements Converter<Integer> {
    @Override
    public Integer toJavaType(String textField) {
        return Integer.valueOf(Optional.ofNullable(textField).orElse("0"));
    }

    @Override
    public String fromJavaType(Integer javaField) {
        return Integer.toString(javaField);
    }
}
