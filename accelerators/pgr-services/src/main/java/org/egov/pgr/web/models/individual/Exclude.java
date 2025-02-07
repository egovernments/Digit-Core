package org.egov.pgr.web.models.individual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// This annotation is used to mark fields in a model class that should be ignored or excluded when the class is being processed,
// specifically during serialization to JSON or when constructing database queries.
public @interface Exclude {
}