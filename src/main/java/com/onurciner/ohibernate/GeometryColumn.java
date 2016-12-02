package com.onurciner.ohibernate;

import com.onurciner.enums.GeometryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Onur.Ciner on 14.11.2016.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeometryColumn {

    String NAME() default "";

    int SRID() default 4326;

    GeometryType GEOMETRY_TYPE() default GeometryType.GEOMETRY;


}
