package com.savdev.commons.config;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class FileConfig implements ConfigFactory {

  public static final String WRONG_METHOD_DECLARATION_FORMAT_MSG =
    "Wrong method declaration. Expected: " + PropertyKey.class.getName() + " annotation. Method name: %s";

  public static final String UNSUPPORTED_METHOD_FORMAT_MSG =
    "Unsupported method: '%s'. Only abstract methods in interfaces are supported.";

  public static final String UNSUPPORTED_RETURN_TYPE_FORMAT_MSG =
    "Unsupported return type: '%s' in method: '%s'.";

  public static final String PROPERTY_DOES_NOT_EXIST_FORMAT_MSG =
    "Property key: '%s' does not exist. Use java.util.Optional as a return type if it is allowed not to have a property.";

  public static final String WRONG_MAP_CONFIG_FORMAT_MSG =
    "Wrong configuration of map items. Expected ${key}%s${value} configuration. Real pair: '%s'";

  public static final String WRONG_PROXY_CONFIG_FORMAT_MSG =
    "Configuration interface is not valid. Errors: %s";

  final InputStream inputStream;

  FileConfig(InputStream inputStream) {
    this.inputStream = inputStream;
  }


  @SuppressWarnings("unchecked")
  @Override
  public <T> T proxy(Class<T> configInterface) {
    try {
      Properties properties = new Properties();
      properties.load(inputStream);
      Map<String, String> propsAsMap = (Map) properties;
      validateProxy(propsAsMap, configInterface);
      return (T) Proxy.newProxyInstance(
        FileConfig.class.getClassLoader(),
        new Class[]{configInterface},
        (proxy, method, methodArgs) -> {
          if (Modifier.isAbstract(method.getModifiers())) {
            PropertyKey propertyKey = method.getAnnotation(PropertyKey.class);
            if (propertyKey == null) {
              throw new IllegalStateException(String.format(WRONG_METHOD_DECLARATION_FORMAT_MSG, method.getName()));
            }

            Class<?> returnType = method.getReturnType();
            boolean isOptional = Optional.class.equals(returnType);
            if (!propsAsMap.containsKey(propertyKey.value())
              && !(isOptional || List.class.equals(returnType) || Map.class.equals(returnType))) {
              throw new IllegalStateException(
                String.format(PROPERTY_DOES_NOT_EXIST_FORMAT_MSG, propertyKey.value()));
            }

            String value = propsAsMap.get(propertyKey.value());
            if (String.class.equals(returnType)) {

              return value;

            } else if (isOptional && String.class.equals(propertyKey.optionalClass())) {

              return value == null ? Optional.empty() : Optional.of(value);

            } else if (int.class.equals(returnType)
              || Integer.class.equals(returnType)) {

              return Integer.valueOf(value);

            } else if (isOptional && Integer.class.equals(propertyKey.optionalClass())) {

              return value == null ? Optional.empty() : Optional.of(Integer.valueOf(value));

            } else if (List.class.equals(returnType)) {

              return value == null ? Collections.emptyList() : Arrays.asList(value.split(propertyKey.itemsSeparator()));

            } else if (Map.class.equals(returnType)) {

              return mapAsReturnValue(method, value);

            } else {

              throw new UnsupportedOperationException(
                String.format(UNSUPPORTED_RETURN_TYPE_FORMAT_MSG,
                  returnType.getName(),
                  method.getName()));
            }

          } else {
            throw new UnsupportedOperationException(
              String.format(UNSUPPORTED_METHOD_FORMAT_MSG, method.getName()));
          }
        });
    } catch (IOException e) {
      throw new IllegalStateException("Could not load file to create file proxy", e);
    }
  }

  private Map<?, ?> mapAsReturnValue(
    final Method method,
    final String value) {
    PropertyKey propertyKey = method.getAnnotation(PropertyKey.class);
    ParameterizedType mapType = (ParameterizedType) method.getGenericReturnType();
    String valueTypeName = mapType.getActualTypeArguments()[1].getTypeName();

    if (String.class.getCanonicalName().equalsIgnoreCase(valueTypeName)) {
      return value == null ? Collections.emptyMap()
        : extractMap(value, propertyKey.itemsSeparator(), propertyKey.keyValueSeparator());
    } else if (valueTypeName.startsWith(List.class.getCanonicalName())) {
      return value == null ? Collections.emptyMap()
        : extractMapOfLists(
        value,
        propertyKey.itemsSeparator(),
        propertyKey.keyValueSeparator(),
        propertyKey.mapsListItemsSeparator()
      );
    } else if (valueTypeName.equals(
      String.format("%s<%s, %s>",
        Map.class.getCanonicalName(),
        String.class.getCanonicalName(),
        String.class.getCanonicalName()))) {
      return value == null ? Collections.emptyMap()
        : Arrays.stream(value.split(propertyKey.itemsSeparator()))
                .collect(toMap(
                  keyValuePair -> validateAndExtract(keyValuePair, propertyKey.keyValueSeparator(), 0),
                  keyValuePair ->
                    extractMap(
                      validateAndExtract(keyValuePair, propertyKey.keyValueSeparator(), 1),
                      propertyKey.mapsListItemsSeparator(),
                      propertyKey.mapsOfMapsItemsSeparator())
                ));
    } else if (valueTypeName.equals(
      String.format("%s<%s, %s<%s>>",
        Map.class.getCanonicalName(),
        String.class.getCanonicalName(),
        List.class.getCanonicalName(),
        String.class.getCanonicalName()))) {
      return value == null ? Collections.emptyMap()
        : Arrays.stream(value.split(propertyKey.itemsSeparator()))
                .collect(toMap(
                  keyValuePair -> validateAndExtract(keyValuePair, propertyKey.keyValueSeparator(), 0),
                  keyValuePair -> extractMapOfLists(
                    validateAndExtract(keyValuePair, propertyKey.keyValueSeparator(), 1),
                    propertyKey.mapsListItemsSeparator(),
                    propertyKey.mapsOfMapsItemsSeparator(),
                    propertyKey.mapsOfMapsOfListsItemsSeparator())
                ));
    } else {
      throw new IllegalStateException("Could not create map from the current configuration: '"
        + value + "'. Method: '" + method.getName() + "'");
    }
  }

  private <T> void validateProxy(
    final Map<String, String> propsAsMap,
    final Class<T> configInterface) {

    List<String> errors = Arrays.stream(configInterface.getMethods())
                                .map(method -> {
                                  if (!Modifier.isAbstract(method.getModifiers())) {
                                    return String.format(UNSUPPORTED_METHOD_FORMAT_MSG, method.getName());
                                  }

                                  PropertyKey propertyKey = method.getAnnotation(PropertyKey.class);
                                  if (propertyKey == null) {
                                    return String.format(WRONG_METHOD_DECLARATION_FORMAT_MSG, method.getName());
                                  }

                                  Class<?> returnType = method.getReturnType();
                                  boolean isOptional = Optional.class.equals(returnType);
                                  if (!propsAsMap.containsKey(propertyKey.value())
                                    && !(isOptional || List.class.equals(returnType) || Map.class.equals(returnType))) {
                                    return String.format(PROPERTY_DOES_NOT_EXIST_FORMAT_MSG, propertyKey.value());
                                  }

                                  //if non of the listed boolean statements is true
                                  if (!(String.class.equals(returnType)
                                    || isOptional && String.class.equals(propertyKey.optionalClass())
                                    || int.class.equals(returnType)
                                    || Integer.class.equals(returnType)
                                    || isOptional && Integer.class.equals(propertyKey.optionalClass())
                                    || List.class.equals(returnType)
                                    || Map.class.equals(returnType))) {
                                    return String.format(UNSUPPORTED_RETURN_TYPE_FORMAT_MSG,
                                      returnType.getName(),
                                      method.getName());
                                  }

                                  return null;
                                }).filter(error -> !StringUtils.isEmpty(error))
                                .collect(Collectors.toList());

    if (!errors.isEmpty()) {
      throw new IllegalStateException(
        String.format(WRONG_PROXY_CONFIG_FORMAT_MSG, String.join(",", errors)));
    }
  }

  private List<String> extractList(final String value, final String separator) {
    return Arrays.stream(value.split(separator)).collect(toList());
  }

  private Map<String, String> extractMap(final String value, final String itemsSeparator, final String keyValueSeparator) {
    return Arrays
      .stream(value.split(itemsSeparator))
      .collect(toMap(
        keyValuePair -> validateAndExtract(keyValuePair, keyValueSeparator, 0),
        keyValuePair -> validateAndExtract(keyValuePair, keyValueSeparator, 1)));
  }

  private Map<String, List<String>> extractMapOfLists(
    final String value,
    final String mapsItemsSeparator,
    final String keyValueSeparator,
    final String mapsListValuesSeparator) {
    return Arrays.stream(value.split(mapsItemsSeparator))
                 .collect(toMap(
                   keyValuePair -> validateAndExtract(keyValuePair, keyValueSeparator, 0),
                   keyValuePair ->
                     extractList(
                       validateAndExtract(
                         keyValuePair, keyValueSeparator, 1),
                       mapsListValuesSeparator)));
  }

  private String validateAndExtract(String keyValuePair, String keyValueSeparator, int position) {
    if (position < 0 || position > 1) {
      throw new IllegalStateException("Wrong API usage. Not correct position. " +
        "For key->value pair expected only 0 for a key and 1 for a value");
    }
    String[] keyValue = keyValuePair.split(keyValueSeparator);
    if (keyValue.length != 2) {
      throw new IllegalStateException(
        String.format(WRONG_MAP_CONFIG_FORMAT_MSG,
          keyValueSeparator,
          keyValuePair));
    }
    return keyValue[position];
  }
}
