package com.pickleball.app.dto.timeslot;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SlotDurationValidator implements ConstraintValidator<ValidSlotDuration, Integer> {

    @Override
    public void initialize(ValidSlotDuration constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value == 30 || value == 60;
    }
}
