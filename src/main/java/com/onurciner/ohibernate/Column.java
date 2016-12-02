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

    /*
    Tablodaki kolonun unique(benzersiz) yapar. Benzeri bir veri gönderilirse hata döner.
     */
    boolean UNIQUE() default false;

    /*
    Db'deki tabloyu not null özelliği ekler. Bu özelliği kullanan field set edilmeden gönderilemez.
     */
    boolean NOTNULL() default false;

    /*
    İlgili kolunun maksimum değerini 1 arttırarak kayıt eder.
     */
    boolean AUTO_INCREMENT_NUMBER() default false;

    /*
    İlgili koluna random integer değer kayıt eder. 899 milyon veriye kadar unique olma garantisi vardır. 899 milyon veriden sonra 0(sıfır) değeri atanır.
     */
    boolean AUTO_RANDOM_NUMBER() default false;
}
