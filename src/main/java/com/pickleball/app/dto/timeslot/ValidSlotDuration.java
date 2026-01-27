package com.pickleball.app.dto.timeslot;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SlotDurationValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSlotDuration {
    String message() default "Slot duration must be 30 or 60 minutes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
