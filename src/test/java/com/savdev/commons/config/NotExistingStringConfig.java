package com.savdev.commons.config;

import static com.savdev.commons.config.TestPropertiesConfig.NOT_EXISTING_PROP_KEY;

public interface NotExistingStringConfig {

  @PropertyKey(NOT_EXISTING_PROP_KEY)
  String notExistingProperty();
}
