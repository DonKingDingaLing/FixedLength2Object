package de.donkingdingaling.tools.fixedlength2object.processors;

import de.donkingdingaling.tools.fixedlength2object.annotations.*;
import de.donkingdingaling.tools.fixedlength2object.converter.Converter;
import de.donkingdingaling.tools.fixedlength2object.exception.ConversionException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LengthFieldProcessor<TYPE> {
    private final Class<?> type;
    private List<Field> sortedLengthFields;

    public LengthFieldProcessor(Class<?> type) {
        this.type = type;
        initializeLengthFields();
    }

    public String fromJavaObject(TYPE object) throws ReflectiveOperationException, IntrospectionException {
        if (isFixedLengthEntity()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Field x : sortedLengthFields) {
                fromJavaField(x, object, stringBuilder);
            }

            return stringBuilder.toString();
        } else {
            throw new IllegalArgumentException("No fixed length type");
        }
    }

    public TYPE toJavaObject(String fixedLengthRow) throws ReflectiveOperationException, IntrospectionException {
        if (isFixedLengthEntity()) {
            TYPE instance = createInstance();

            int start = 0;

            for (Field field : sortedLengthFields) {
                toJavaField(field, start, instance, fixedLengthRow);
                start += getLength(field, instance);
            }

            return instance;
        } else {
            throw new IllegalArgumentException("No fixed length type");
        }
    }

    private void fromJavaField(Field field, TYPE instance, StringBuilder builder) throws ReflectiveOperationException, IntrospectionException {
        int length = getLength(field, instance);
        Converter converter = getConverter(field);
        String stringValue = null;

        try {
            Object fieldValue = new PropertyDescriptor(field.getName(), type).getReadMethod().invoke(instance);
            stringValue = converter.fromJavaType(fieldValue);
        } catch (IllegalAccessException | ConversionException | IntrospectionException e) {
            throw new IllegalArgumentException("Could not set value on field" + field.getName());
        }

        if (stringValue.length() > length) {
            throw new IllegalArgumentException("Field length out of bounds");
        }

        applyPadding(field, builder, length, stringValue);
    }

    private void toJavaField(Field field, Integer start, TYPE instance, String fixedLengthRow) {
        try {
            Object value = toJavaFieldValue(field, fixedLengthRow, start, instance);
            new PropertyDescriptor(field.getName(), type).getWriteMethod().invoke(instance, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not set value on field" + field.getName());
        }
    }

    private Object toJavaFieldValue(Field field, String fixedLengthRow, Integer start, TYPE instance) throws ReflectiveOperationException, ConversionException, IntrospectionException {
        int length = getLength(field, instance);
        String stringValue = fixedLengthRow.substring(start, start + length).trim();
        Converter converter = getConverter(field);
        return converter.toJavaType(stringValue);
    }

    private TYPE createInstance() {
        try {
            return (TYPE) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("No accessible constructor declared in type: " + type.getName());
        }
    }

    private boolean isFixedLengthEntity() {
        return type.isAnnotationPresent(LengthEntity.class);
    }

    private int getLength(Field field, TYPE instance) throws ReflectiveOperationException, IntrospectionException {
        if (field.isAnnotationPresent(DynamicLengthField.class)) {
            DynamicLengthField dynamicLengthField = field.getAnnotation(DynamicLengthField.class);
            String lengthFieldName = dynamicLengthField.lengthField();
            return (int) new PropertyDescriptor(lengthFieldName, type).getReadMethod().invoke(instance);

        } else {
            FixedLengthField fixedLengthField = field.getAnnotation(FixedLengthField.class);
            return fixedLengthField.length();
        }
    }

    private void initializeLengthFields() {
        if (sortedLengthFields == null) {
            Field[] fields = type.getDeclaredFields();

            sortedLengthFields = Arrays.stream(fields)
                    .filter(LengthFieldProcessor::isLengthField)
                    .sorted(LengthFieldProcessor::sortByOrder)
                    .collect(Collectors.toList());
        }
    }

    private static void applyPadding(Field field, StringBuilder builder, int length, String stringValue) {
        int n = length - stringValue.length();
        String paddingString = IntStream.range(0, n)
                .mapToObj(i -> String.valueOf(getPadding(field).paddingCharacter()))
                .collect(Collectors.joining(""));

        if (getPadding(field).alignment() == Alignment.LEFT) {
            builder.append(stringValue)
                    .append(paddingString);
        } else {
            builder.append(paddingString)
                    .append(stringValue);
        }
    }

    private static int sortByOrder(Field firstFixedLengthField, Field secondFixedLengthField) {
        int firstOrder = firstFixedLengthField.getAnnotation(FixedLengthField.class).order();
        int secondOrder = secondFixedLengthField.getAnnotation(FixedLengthField.class).order();

        return Integer.compare(firstOrder, secondOrder);
    }

    private static Converter getConverter(Field field) throws ReflectiveOperationException {
        TypeInformation typeInformation = getTypeInformation(field);
        return typeInformation.converter().newInstance();
    }

    private static boolean isLengthField(Field field) {
        return field.isAnnotationPresent(FixedLengthField.class)
                || field.isAnnotationPresent(DynamicLengthField.class);
    }

    private static Padding getPadding(Field field) {
        if (field.isAnnotationPresent(Padding.class)) {
            return field.getAnnotation(Padding.class);
        } else if (field.isAnnotationPresent(DynamicLengthField.class)) {
            DynamicLengthField dynamicLengthField = field.getAnnotation(DynamicLengthField.class);
            return dynamicLengthField.padding();
        } else {
            FixedLengthField fixedLengthField = field.getAnnotation(FixedLengthField.class);
            return fixedLengthField.padding();
        }
    }

    private static TypeInformation getTypeInformation(Field field) {
        if (field.isAnnotationPresent(TypeInformation.class)) {
            return field.getAnnotation(TypeInformation.class);
        } else if (field.isAnnotationPresent(DynamicLengthField.class)) {
            DynamicLengthField dynamicLengthField = field.getAnnotation(DynamicLengthField.class);
            return dynamicLengthField.typeInformation();
        } else {
            FixedLengthField fixedLengthField = field.getAnnotation(FixedLengthField.class);
            return fixedLengthField.typeInformation();
        }
    }
}
