package de.donkingdingaling.tools.fixedlength2object.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Padding {
    Alignment alignment() default Alignment.RIGHT;
    char paddingCharacter() default ' ';
}
