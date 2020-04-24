##### Usage:

###### 1 Create a properties file:
```
test.key1=test.value1
test.key2=test.value2
test.key4=23
test.key.list=item1|item2|item3
test.key.list.custom=item1,item2,item3
test.key.map=key1->value1|key2->value2|key3->value3
test.key.custom.map=key1&value1|key2&value2
```

###### 2 Define config interface
```
public interface Config {

  @PropertyKey("test.key1")
  String someProperty1();

  @PropertyKey("test.key2")
  Optional<String> someProperty2ViaOptional();

  @PropertyKey("test.key4"")
  int intProperty();

  @PropertyKey(value = "test.key4", optionalClass = Integer.class)
  Optional<Integer> intProperty2ViaOptional();

  @PropertyKey("test.key.list")
  List<String> listDefaultSeparator();

  @PropertyKey(value = "test.key.list.custom", itemsSeparator = ",")
  List<String> listCustomSeparator();

  @PropertyKey("test.key.map")
  Map<String, String> configMap();

  @PropertyKey(value = "test.key.custom.map", keyValueSeparator = "&")
  Map<String, String> customMap();
}
```

###### 3 Get a config proxy:
From `InputStream` object, useful in tests:
```
Config config = Configs.fileConfig(getInputStreamFromPropertyFile(""))
      .proxy(TestPropertiesConfig.class);
```
From a file, located on the path, managed by a combination of
 a system property/variable, that refers to the folder location and a file name:
```
Config config = Configs.fileConfig(SYSTEM_VARIABLE_NAME, PROP_FILE_NAME)
      .proxy(TestPropertiesConfig.class);
```


###### 4 Use a config proxy:
```


// returns 'test.value1'     
String value = config.someProperty1(); 
      
InputStream getInputStreamFromPropertyFile(String fileName) { ... }
```