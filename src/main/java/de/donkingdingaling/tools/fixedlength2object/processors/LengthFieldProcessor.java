package de.donkingdingaling.tools.fixedlength2object.processors;

import de.donkingdingaling.tools.fixedlength2object.annotations.*;
import de.donkingdingaling.tools.fixedlength2object.converter.Converter;
import de.donkingdingaling.tools.fixedlength2object.exception.ConversionException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LengthFieldProcessor<TYPE> {
    private Class<?> type;
    private Map<Integer, Integer> orderToStart;
    private List<Field> sortedLengthFields;

    public LengthFieldProcessor(Class<?> type) {
        this.type = type;
    }

    public String fromJavaObject(TYPE object) {
        if(isFixedLengthEntity()) {
            StringBuilder stringBuilder = new StringBuilder();
            initializeLengthFields();
            sortedLengthFields.forEach(x -> writeValueToString(x, object, stringBuilder));

            return stringBuilder.toString();
        } else {
            throw new IllegalArgumentException("No fixed length type");
        }
    }

    public TYPE toJavaObject(String fixedLengthRow) {
        if(isFixedLengthEntity(type)) {
            TYPE instance = createInstance();

            Field[] fields = type.getDeclaredFields();
            
            fields = Arrays.stream(fields)
                    .filter(this::isLengthField)
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

    private void writeValueToString(Field field, TYPE instance, StringBuilder builder) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        determineFieldString(field, instance, builder);

        field.setAccessible(accessible);

    }

    private void writeValueToField(Field field, Integer start, TYPE instance, String fixedLengthRow) {
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

    private void determineFieldString(Field field, TYPE instance, StringBuilder builder) throws ReflectiveOperationException {
        int length = getLength();
        TypeInformation typeInformation = getTypeInformation(field);
        Converter converter = typeInformation.converter().newInstance();
        FixedLengthField fieldAnnotation = field.getAnnotation(FixedLengthField.class);
        String stringValue = null;

        try {
            stringValue = converter.fromJavaType(field.get(instance));
        } catch (IllegalAccessException | ConversionException e) {
            throw new IllegalArgumentException("Could not set value on field" + field.getName());
        }

        if(stringValue.length() > length) {
            throw new IllegalArgumentException("Field length out of bounds");
        }

        int n = length - stringValue.length();
        String paddingString = IntStream.range(0, n)
                .mapToObj(i -> String.valueOf(getPadding(field).paddingCharacter()))
                .collect(Collectors.joining(""));

        if(getPadding(field).alignment() == Alignment.LEFT) {
            builder.append(paddingString)
                    .append(stringValue);
        } else {
            builder.append(stringValue)
                    .append(paddingString);
        }
    }

    private TYPE createInstance() {
        try {
            return (TYPE)type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("No accessible constructor declared in type: " + type.getName());
        }
    }

    private boolean isFixedLengthEntity() {
        return type.isAnnotationPresent(LengthEntity.class);
    }

    private boolean isLengthField(Field field) {
        return field.isAnnotationPresent(FixedLengthField.class)
                || field.isAnnotationPresent(DynamicLengthField.class);
    }

    private Padding getPadding(Field field) {
        if(field.isAnnotationPresent(Padding.class)) {
            return field.getAnnotation(Padding.class);
        } else if(field.isAnnotationPresent(DynamicLengthField.class)) {
            DynamicLengthField dynamicLengthField = field.getAnnotation(DynamicLengthField.class);
            return dynamicLengthField.padding();
        } else {
            FixedLengthField fixedLengthField = field.getAnnotation(FixedLengthField.class);
            return fixedLengthField.padding();
        }
    }

    private TypeInformation getTypeInformation(Field field) {
        if(field.isAnnotationPresent(TypeInformation.class)) {
            return field.getAnnotation(TypeInformation.class);
        } else if(field.isAnnotationPresent(DynamicLengthField.class)) {
            DynamicLengthField dynamicLengthField = field.getAnnotation(DynamicLengthField.class);
            return dynamicLengthField.typeInformation();
        } else {
            FixedLengthField fixedLengthField = field.getAnnotation(FixedLengthField.class);
            return fixedLengthField.typeInformation();
        }
    }

    private int getLength() {
        // FIXME Impelment
        return 0;
    }

    private void initializeLengthFields() {
        if(sortedLengthFields == null) {
            Field[] fields = type.getDeclaredFields();

            sortedLengthFields = Arrays.stream(fields)
                    .filter(this::isLengthField)
                    .sorted(this::sortByOrder)
                    .collect(Collectors.toList());
        }
    }
}
