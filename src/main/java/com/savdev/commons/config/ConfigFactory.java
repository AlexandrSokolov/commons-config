package com.savdev.commons.config;

public interface ConfigFactory {

  <T> T proxy(Class<T> configInterface);

}
