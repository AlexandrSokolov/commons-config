package com.savdev.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class ConfigFactoryTest {

  private static final String SINGLE_STRING_PROPERTY = "configs/test.config.properties";
  private static final String STRING_PROP_VALUE1 = "test.value1";
  private static final String STRING_PROP_VALUE2 = "test.value2";
  private static final int INT_PROP_VALUE = 23;
  private static final String NOT_EXISTING_FILE = "not_existing.file";

  @Test
  public void testStrings(){

    TestPropertiesConfig sm = Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
      .proxy(TestPropertiesConfig.class);
    Assert.assertEquals(
      STRING_PROP_VALUE1,
      sm.someProperty1());

    Assert.assertEquals(
      STRING_PROP_VALUE2,
      sm.someProperty2());
  }

  @Test
  public void testOptionalString(){
    Assert.assertEquals(
      STRING_PROP_VALUE2,
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .someProperty2ViaOptional()
        .get());
  }

  @Test
  public void testIntPrimitive(){

    TestPropertiesConfig sm = Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
      .proxy(TestPropertiesConfig.class);
    Assert.assertEquals(
      INT_PROP_VALUE,
      sm.intProperty());
  }

  @Test
  public void testInteger(){

    TestPropertiesConfig sm = Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
      .proxy(TestPropertiesConfig.class);
    Assert.assertEquals(
      (Integer) INT_PROP_VALUE,
      sm.integerProperty());
  }

  @Test
  public void testOptionalInteger(){
    Assert.assertEquals(
      (Integer) INT_PROP_VALUE,
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .intProperty2ViaOptional()
        .get());
  }

  @Test
  public void testListDefaultSeparator(){
    Assert.assertEquals(
      Lists.newArrayList("item1", "item2", "item3"),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .listDefaultSeparator()
    );
  }

  @Test
  public void testListCustomSeparator(){
    Assert.assertEquals(
      Lists.newArrayList("item1", "item2", "item3"),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .listCustomSeparator()
    );
  }

  @Test
  public void testNotExistingList(){
    Assert.assertTrue(
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .emptyList()
        .isEmpty());
  }

  @Test
  public void testMapDefaultSeparator(){
    Assert.assertEquals(
      ImmutableMap.of(
        "key1", "value1",
        "key2", "value2",
        "key3", "value3"),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .defaultMap()
    );
  }

  @Test
  public void testMapCustomSeparator(){
    Assert.assertEquals(
      ImmutableMap.of(
        "key1", "value1",
        "key2", "value2"),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .customMap()
    );
  }

  @Test
  public void testEmptyMap(){
    Assert.assertTrue(
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .notExistingMap()
        .isEmpty()
    );
  }

  @Test
  public void testMapDefaultOfLists(){
    Assert.assertEquals(
      ImmutableMap.of(
        "key1", Lists.newArrayList("value1", "value2", "value3"),
        "key2", Lists.newArrayList("value2"),
        "key3", Lists.newArrayList("value4", "value5")),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
             .proxy(TestPropertiesConfig.class)
             .defaultMapOfLists()
    );
  }

  @Test
  public void testMapDefaultOfMaps(){
    Assert.assertEquals(
      ImmutableMap.of(
        "key1", ImmutableMap.of("subkey1", "value1", "subkey2", "value2"),
        "key2", ImmutableMap.of("subkey3", "value2"),
        "key3", ImmutableMap.of("subkey4", "value4", "subkey5", "value5")),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
             .proxy(TestPropertiesConfig.class)
             .defaultMapOfMaps()
    );
  }

  @Test
  public void testMapDefaultOfMapsOfLists(){
    Assert.assertEquals(
      ImmutableMap.of(
        "key1", ImmutableMap.of(
          "subkey1",
              Lists.newArrayList("value1", "value2", "value3"),
          "subkey2",
              Lists.newArrayList("value2_1", "value2_2")),
        "key2", ImmutableMap.of(
          "subkey3", Collections.singletonList("value2")),
        "key3", ImmutableMap.of(
          "subkey4",
          Lists.newArrayList("value4_1", "value4_2"),
          "subkey5",
          Collections.singletonList("value5"))),
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
             .proxy(TestPropertiesConfig.class)
             .defaultMapOfMapsOfLists()
    );
  }

  @Test
  public void testNotExistingProperty(){

    try {
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(NotExistingStringConfig.class);
      Assert.fail();
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(FileConfig.WRONG_PROXY_CONFIG_FORMAT_MSG,
          String.format(FileConfig.PROPERTY_DOES_NOT_EXIST_FORMAT_MSG, TestPropertiesConfig.NOT_EXISTING_PROP_KEY)),
        e.getMessage());
    }
  }

  @Test
  public void testNotExistingPropertyAsOptional(){

    Assert.assertFalse(
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(TestPropertiesConfig.class)
        .notExistingPropertyAsOptionalString()
        .isPresent());
  }

  @Test
  public void testUnsupportedReturnType(){

    try {
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(UnsupportedReturnTypeConfig.class);
      Assert.fail();
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(FileConfig.WRONG_PROXY_CONFIG_FORMAT_MSG,
          String.format(
            FileConfig.UNSUPPORTED_RETURN_TYPE_FORMAT_MSG,
            Collections.class.getName(),
            "unsupportedReturnType")),
        e.getMessage());
    }
  }

  @Test
  public void testNotExistingFile(){
    try {
      Configs.fileConfig(new FileInputStream(NOT_EXISTING_FILE))
        .proxy(TestPropertiesConfig.class);
      Assert.fail();
    } catch (Exception e){
      Assert.assertEquals(FileNotFoundException.class, e.getClass());
      Assert.assertTrue(e.getMessage().contains(NOT_EXISTING_FILE));
    }
  }


  @Test
  public void testDefaultMethod(){
    try {
      Configs.fileConfig(testInputStream(SINGLE_STRING_PROPERTY))
        .proxy(DefaultMethodConfig.class);
      Assert.fail();
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(FileConfig.WRONG_PROXY_CONFIG_FORMAT_MSG,
          String.format(FileConfig.UNSUPPORTED_METHOD_FORMAT_MSG,"someDefault")),
        e.getMessage());
    }
  }

  private InputStream  testInputStream(String filePath) {
    try {
      String withoutFirstSlash = filePath.startsWith(File.separator) ?
        filePath.substring(File.separator.length()) : filePath;

      URL pathResource = ConfigFactoryTest.class
        .getClassLoader()
        .getResource(withoutFirstSlash);
      if (pathResource == null) {
        throw new IllegalStateException(
          "Could not find resource for the path in test resources: [" + filePath + "]");
      }
      String absolutePath = new File(pathResource.toURI()).getAbsolutePath();
      return new FileInputStream(absolutePath);
    } catch (Exception e){
      throw new IllegalStateException(e);
    }
  }
}
