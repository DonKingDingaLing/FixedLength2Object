package de.donkingdingaling.tools.fixedlength2object.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FixedLengthField {
    int order();
    int length();
    Padding padding() default Padding.LEFT;
    char paddingCharacter() default ' ';
    String format() default "";
}
