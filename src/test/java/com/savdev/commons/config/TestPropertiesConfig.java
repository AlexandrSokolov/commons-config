package com.savdev.commons.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestPropertiesConfig {

  String STRING_PROP_KEY1 = "test.key1";
  String STRING_PROP_KEY2 = "test.key2";
  String INT_PROP_KEY4 = "test.key4";
  String LIST_ITEMS_KEY = "test.key.list";
  String LIST_ITEMS_KEY_CUSTOM = "test.key.list.custom";
  String MAP_ITEMS_KEY = "test.key.map";
  String MAP_ITEMS_KEY_CUSTOM_SEPARATOR = "test.key.custom.map";
  String NOT_EXISTING_PROP_KEY = "test.not.existing.key";

  @PropertyKey(STRING_PROP_KEY1)
  String someProperty1();

  @PropertyKey(STRING_PROP_KEY2)
  String someProperty2();

  @PropertyKey(NOT_EXISTING_PROP_KEY)
  Optional<String> notExistingPropertyAsOptionalString();

  @PropertyKey(STRING_PROP_KEY2)
  Optional<String> someProperty2ViaOptional();

  @PropertyKey(INT_PROP_KEY4)
  int intProperty();

  @PropertyKey(INT_PROP_KEY4)
  Integer integerProperty();

  @PropertyKey(value = INT_PROP_KEY4, optionalClass = Integer.class)
  Optional<Integer> intProperty2ViaOptional();

  @PropertyKey(LIST_ITEMS_KEY)
  List<String> listDefaultSeparator();

  @PropertyKey(value = LIST_ITEMS_KEY_CUSTOM, itemsSeparator = ",")
  List<String> listCustomSeparator();

  @PropertyKey(NOT_EXISTING_PROP_KEY)
  List<String> emptyList();

  @PropertyKey(MAP_ITEMS_KEY)
  Map<String, String> defaultMap();

  @PropertyKey(value = MAP_ITEMS_KEY_CUSTOM_SEPARATOR, keyValueSeparator = "&")
  Map<String, String> customMap();

  @PropertyKey(NOT_EXISTING_PROP_KEY)
  Map<String, String> notExistingMap();

}
