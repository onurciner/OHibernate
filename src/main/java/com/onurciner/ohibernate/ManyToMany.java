package com.onurciner.ohibernate;


import com.onurciner.enums.CascadeType;
import com.onurciner.enums.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Onur.Ciner on 7.11.2016.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {

//   String JoinColumn() default "";

   String Key() default "";

   CascadeType[] Cascade() default CascadeType.ALL;

   FetchType Fetch() default FetchType.EAGER;

}
