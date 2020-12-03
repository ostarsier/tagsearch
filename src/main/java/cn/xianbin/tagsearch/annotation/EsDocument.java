package cn.xianbin.tagsearch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EsDocument {

    /**
     * 索引名称
     */
    String indexName();

    /**
     * type名称
     */
    String type() default "";


}