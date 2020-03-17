package com.savdev.commons.config;

public interface DefaultMethodConfig {

  String DEFAULT_VALUE = "default.value";

  default String someDefault(){
    return DEFAULT_VALUE;
  }
}
