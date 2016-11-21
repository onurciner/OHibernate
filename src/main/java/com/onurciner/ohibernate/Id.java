package com.onurciner.ohibernate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Onur.Ciner on 8.11.2016.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    /*
    field ile db'deki kolonun adı farklı ise bu tag ile db'deki kolonun adı yazılır.
    */
    String NAME() default "";

    /*
    ID kolonuna otomatik olarak id atılmasını istersek true yapmamız yeterli.
    Dikkat tabloya autoincrement tanımlanmamalı.
     */
    boolean AUTO_ID() default false;

    /*
    ID negatif olarak atılmasını istersek true yapmamız yeterli.
     */
    boolean NEGATIVE() default false;

    /**
     * AUTO_ID ile aynı anda kullanılmaması gerekmektedir.
     * ID'nin mutlaka INTEGER olması gerekmektedir. Aksi taktirde hata çıkar.
     * @return default olarak false.
     * id sürekli pozitif olarak artım gösterir. ID'nin negatif veya değişkenlik gösterebilmesi için AUTO_ID kullanınız.
     */
    boolean PRIMERY_KEY_AUTOINCREMENT() default false;
}
