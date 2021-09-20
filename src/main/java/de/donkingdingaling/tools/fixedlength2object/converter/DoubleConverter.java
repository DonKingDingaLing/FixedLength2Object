package de.donkingdingaling.tools.fixedlength2object.converter;

public class DoubleConverter implements Converter<Double> {
    @Override
    public Double toJavaType(String textField) {
        if(textField != null && !textField.isEmpty()) {
            return Double.valueOf(textField);
        }
        return 0.0;
    }

    @Override
    public String fromJavaType(Double javaField) {
        return Double.toString(javaField);
    }
}
