package io.baardl.iot.azure.utils;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class Configuration {
    private static final Logger log = getLogger(Configuration.class);

    public static Properties readLocalProperties() {
        String overloadFile = "azure_iot_mqtt.properties";
        String rootPath = Paths.get("").toAbsolutePath().toString();
        String appConfigPath = rootPath + "/" + overloadFile;

        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
        } catch (Exception e) {
            log.error("Failed to load required properties file: {}. Reason: {}", appConfigPath, e.getMessage());
        }
        return appProps;
    }

    public static String readProperty(String key) {
        String value = readLocalProperties().getProperty(key);
        return value;
    }
}
