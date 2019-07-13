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

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import javax.annotation.meta.TypeQualifierNickname;
import java.lang.annotation.*;

/**
 * This annotation can be applied to a class to indicate that the method
 * parameters and return values are nonnull by default unless there is
 * an explicit nullness annotation on the element that overrides it
 */
@Documented
@LibraryAnnotation
@TypeQualifierDefault({ ElementType.METHOD, ElementType.PARAMETER })
@TypeQualifierNickname @Nonnull
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MethodsNotNull {}
