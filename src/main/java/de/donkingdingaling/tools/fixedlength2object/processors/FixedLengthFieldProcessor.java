package de.donkingdingaling.tools.fixedlength2object.processors;

import de.donkingdingaling.tools.fixedlength2object.annotations.FixedLengthEntity;
import de.donkingdingaling.tools.fixedlength2object.annotations.FixedLengthField;

import java.lang.reflect.Field;
import java.util.Arrays;

public class FixedLengthFieldProcessor<Type> {
    private Class type;

    public FixedLengthFieldProcessor(Class type) {
        this.type = type;
    }

    public Type process(String fixedLengthRow) {
        if(isFixedLengthEntity(type)) {
            Type instance = createInstance();

            Field[] fields = type.getDeclaredFields();

            Arrays.stream(fields)
                    .filter(this::isFixedLengthField)
                    .sorted((x,y) -> (x.getAnnotation(FixedLengthField.class).order() < y.getAnnotation(FixedLengthField.class).order()))
                    .forEach(x -> processSingleField(x, instance, fixedLengthRow));

            return instance;
        } else {
            throw new IllegalArgumentException("No fixed length type");
        }
    }

    private void processSingleField(Field field, Type instance, String fixedLengthRow) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        Object vaule = determineFieldValue(field, fixedLengthRow);

        field.set(instance, value);

        field.setAccessible(accessible);
    }

    private Object determineFieldValue(Field field, String fixedLengthRow) {
        FixedLengthField fieldAnnotation = field.getAnnotation(FixedLengthField.class);
        fieldAnnotation.
    }

    private Type createInstance() {
        try {
            Type instance = (Type)type.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException e) {
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
