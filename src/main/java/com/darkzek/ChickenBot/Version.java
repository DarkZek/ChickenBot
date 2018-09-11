package com.darkzek.ChickenBot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static String version;

    public static String getVersion() {
        if (version == null) {
            version = findVersion();
        }

        return version;
    }

    private static String findVersion()
    {
        String path = "/version.prop";
        InputStream stream = new Version().getClass().getResourceAsStream(path);
        if (stream == null)
            return "UNKNOWN";
        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "UNKNOWN VERSION";
        }
    }

}
