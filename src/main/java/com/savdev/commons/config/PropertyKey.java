package com.savdev.commons.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target( {METHOD} )
public @interface PropertyKey {

  String ITEMS_SEPARATOR = "\\|";
  String KEY_VALUE_SEPARATOR = "->";

  String value();

  Class<?> optionalClass() default String.class;

  String itemsSeparator() default ITEMS_SEPARATOR;

  String keyValueSeparator() default KEY_VALUE_SEPARATOR;

}
