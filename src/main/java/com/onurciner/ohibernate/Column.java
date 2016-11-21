package com.onurciner.ohibernate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Onur.Ciner on 7.11.2016.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /*
    field ile db'deki kolonun adı farklı ise bu tag ile db'deki kolonun adı yazılır.
     */
    String NAME() default "";

    /*
    Otomatik olarak db tablosundaki ilgili kolona datetime atar. True yapılması gerekir.
     */
    boolean DATETIME() default false;
}
