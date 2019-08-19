package de.donkingdingaling.tools.fixedlength2object.processors;

import de.donkingdingaling.tools.fixedlength2object.annotations.FixedLengthEntity;
import de.donkingdingaling.tools.fixedlength2object.annotations.FixedLengthField;
import de.donkingdingaling.tools.fixedlength2object.annotations.Padding;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FixedLengthFieldProcessor<Type> {
    private Class type;
    private Map<Integer, Integer> orderToStart;
    
    public FixedLengthFieldProcessor(Class type) {
        this.type = type;
    }

    public String objectToString(Type object) {
        if(isFixedLengthEntity(type)) {
            Field[] fields = type.getDeclaredFields();
            StringBuilder stringBuilder = new StringBuilder();

            Arrays.stream(fields)
                    .filter(this::isFixedLengthField)
                    .sorted(this::sortByOrder)
                    .forEach(x -> writeValueToString(x, object, stringBuilder));

            return stringBuilder.toString();
        } else {
            throw new IllegalArgumentException("No fixed length type");
        }
    }

    public Type stringToObject(String fixedLengthRow) {
        if(isFixedLengthEntity(type)) {
            Type instance = createInstance();

            Field[] fields = type.getDeclaredFields();
            
            fields = Arrays.stream(fields)
                    .filter(this::isFixedLengthField)
                    .sorted(this::sortByOrder)
                    .toArray(Field[]::new);
            
            int start = 0;
            
            for(Field field : fields) {
                writeValueToField(field, start, instance, fixedLengthRow);
                start += field.getAnnotation(FixedLengthField.class).length();
            }

            return instance;
        } else {
            throw new IllegalArgumentException("No fixed length type");
        }
    }
    
    private int sortByOrder(Field firstFixedLengthField, Field secondFixedLengthField) {
        int firstOrder = firstFixedLengthField.getAnnotation(FixedLengthField.class).order();
        int secondOrder = secondFixedLengthField.getAnnotation(FixedLengthField.class).order();
        
        return firstOrder < secondOrder ? -1 : 1;
    }

    private void writeValueToString(Field field, Type instance, StringBuilder builder) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        determineFieldString(field, instance, builder);

        field.setAccessible(accessible);

    }

    private void writeValueToField(Field field, Integer start, Type instance, String fixedLengthRow) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        Object value = determineFieldValue(field, fixedLengthRow, start);

        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not set value on field" + field.getName());
        }

        field.setAccessible(accessible);
    }

    private Object determineFieldValue(Field field, String fixedLengthRow, Integer start) {
        FixedLengthField fieldAnnotation = field.getAnnotation(FixedLengthField.class);
        Class fieldType = field.getType();
        String stringValue = fixedLengthRow.substring(start, start + fieldAnnotation.length()).trim();
        Optional<String> value = Optional.ofNullable(stringValue.isEmpty() ? null : stringValue);

        if(String.class.isAssignableFrom(fieldType)) {
            return stringValue;
        } else if(Long.TYPE.isAssignableFrom(fieldType)) {
            return Long.valueOf(value.orElse("0"));
        } else if(Integer.TYPE.isAssignableFrom(fieldType)) {
            return Integer.valueOf(value.orElse("0"));
        } else if(Short.TYPE.isAssignableFrom(fieldType)) {
            return Short.valueOf(value.orElse("0"));
        } else if(Byte.TYPE.isAssignableFrom(fieldType)) {
            return Byte.valueOf(value.orElse("0"));
        } else if(Double.TYPE.isAssignableFrom(fieldType)) {
            return Double.valueOf(value.orElse("0"));
        } else if(Float.TYPE.isAssignableFrom(fieldType)) {
            return Float.valueOf(value.orElse("0"));
        } else {
            throw new IllegalArgumentException("Invalid or non primitive value");
        }
    }

    private void determineFieldString(Field field, Type instance, StringBuilder builder) {
        FixedLengthField fieldAnnotation = field.getAnnotation(FixedLengthField.class);
        String stringValue = null;

        try {
            stringValue = String.valueOf(field.get(instance));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not set value on field" + field.getName());
        }

        if(stringValue.length() > fieldAnnotation.length()) {
            throw new IllegalArgumentException("Field lentgh out of bounds");
        }

        int n = fieldAnnotation.length() - stringValue.length();
        String paddingString = IntStream.range(0, n)
                .mapToObj(i -> String.valueOf(fieldAnnotation.paddingCharacter()))
                .collect(Collectors.joining(""));

        if(fieldAnnotation.padding() == Padding.LEFT) {
            stringValue = paddingString + stringValue;
        } else {
            stringValue += paddingString;
        }
        builder.append(stringValue);
    }

    private Type createInstance() {
        try {
            return (Type)type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("No accessible constructor declared in type: " + type.getName());
        }
    }

    private boolean isFixedLengthEntity(Class type) {
        return type.isAnnotationPresent(FixedLengthEntity.class);
    }

    private boolean isFixedLengthField(Field field) {
        return field.isAnnotationPresent(FixedLengthField.class);
    }
}
