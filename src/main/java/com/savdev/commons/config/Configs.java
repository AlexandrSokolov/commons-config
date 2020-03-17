package com.savdev.commons.config;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Configs {

  public static ConfigFactory fileConfig(InputStream inputStream){
    return new FileConfig(inputStream);
  }

  public static ConfigFactory fileConfig(String sysProperty4Folder, String fileName){
    try {
      return new FileConfig(
        new FileInputStream(
          propertiesFilePath(sysProperty4Folder, fileName)));
    } catch (FileNotFoundException e){
      throw new IllegalStateException(e);
    }

  }

  private static String propertiesFilePath(
    final String configFolderSystemProperty,
    final String fileName) {
    if (StringUtils.isEmpty(configFolderSystemProperty)) {
      throw new IllegalStateException(
        "Not nullable parameter for "
          + "system property is expected.");
    }
    String folderViaSetting = System.getProperty(configFolderSystemProperty);
    if (StringUtils.isEmpty(folderViaSetting)) {
      throw new IllegalStateException(
        "System property is not defined: "
          + configFolderSystemProperty);
    }

    File configFolder = new File(folderViaSetting);
    validateDirectory(configFolder);

    if (StringUtils.isEmpty(fileName)) {
      throw new IllegalStateException(
        "Not nullable parameter for "
          + "property file propKey is expected.");
    }
    File configFile = new File(configFolder, fileName);
    validateFile(configFile);

    return configFile.getAbsolutePath();
  }


  private static void validateDirectory(File file) {
    if (!file.exists()) {
      throw new IllegalStateException("Directory not exist: "
        + file.getAbsolutePath());
    }
    if (!file.isDirectory()) {
      throw new IllegalStateException("Directory, not file is expected: "
        + file.getAbsolutePath());
    }
  }

  private static void validateFile(File file) {
    if (!file.exists()) {
      throw new IllegalStateException("File not exist: "
        + file.getAbsolutePath());
    }
    if (file.isDirectory()) {
      throw new IllegalStateException("File, but not directory is expected: "
        + file.getAbsolutePath());
    }
    if (!file.canRead()) {
      throw new IllegalStateException("File exists, but cannot be read: "
        + file.getAbsolutePath());
    }
  }
}
