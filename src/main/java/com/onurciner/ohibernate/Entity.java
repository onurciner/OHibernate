package com.onurciner.ohibernate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Onur.Ciner on 7.11.2016.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
    /*
    Veritabanındaki tablo ismiyle bean'ın isimleri farklı ise o zaman bu tag ile db'deki tablo ismini belirtiyoruz.
     */
    String TABLE_NAME() default "";

    enum TABLE_OPERATION_TYPE {
        DEFAULT, CREATE, DROP_AND_CREATE
    }

    /*
    Veritabanında tablo işlemleri için durum belirleme.
     */
    TABLE_OPERATION_TYPE TABLE_OPERATION() default TABLE_OPERATION_TYPE.DEFAULT;


}
