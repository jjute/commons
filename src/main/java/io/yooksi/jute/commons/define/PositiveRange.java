/*
 * Copyright [2019] [Matthew Cain]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.yooksi.jute.commons.define;

import io.yooksi.jute.commons.validator.PositiveRangeValidator;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * The annotated element must at all times be a number with a positive value
 * that does not exceed the maximum defined value {@code max()}.
 */
@LibraryAnnotation
@TypeQualifierNickname @Nonnull
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, })
@Constraint(validatedBy = { PositiveRangeValidator.class })
@SuppressWarnings("unused")
public @interface PositiveRange {

    /* Declare this as a number type double so it can work with
     * integers, floats and other numbers without losing precision
     */
    double max();

    String level() default "ERROR";

    String message() default "Found invalid number value $value out of valid positive range (0-$max)";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
