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

import io.yooksi.jute.commons.util.AnnotationUtils;
import io.yooksi.jute.commons.validator.BeanValidator;

import java.lang.annotation.*;

/**
 * This annotation is applied to other annotations to indicate
 * that they are custom annotations created by this library and
 * require special processing by {@link BeanValidator} such as
 * message parsing and dynamic level assignment.
 *
 * @see AnnotationUtils#isLibraryAnnotation(Annotation)
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LibraryAnnotation {}
