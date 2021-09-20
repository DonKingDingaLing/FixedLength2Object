package de.donkingdingaling.tools.fixedlength2object.annotations;

import de.donkingdingaling.tools.fixedlength2object.converter.Converter;
import de.donkingdingaling.tools.fixedlength2object.converter.DefaultConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TypeInformation {
    Class<?> type();
    Class<? extends Converter<?>> converter();
    String format() default "";
}
