package com.darkzek.ChickenBot.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class GuildConfigurationManager {

    //Singleton stuff
    private static GuildConfigurationManager singleton = new GuildConfigurationManager( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private GuildConfigurationManager() {
        //Save configs on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SaveConfigs();
        }));

        //Load them on startup!
        LoadConfigs();
    }

    /* Static 'instance' method */
    public static GuildConfigurationManager getInstance( ) {
        return singleton;
    }

    private HashMap<String, GuildConfiguration> configurations = new HashMap();

    public GuildConfiguration GetGuildConfiguration(String id) {

        if (!configurations.containsKey(id)) {
            return new GuildConfiguration(id);
        }

        return configurations.get(id);
    }

    protected void SetGuildConfiguration(GuildConfiguration configuration) {

        if (configurations.containsKey(configuration.guildId)) {
            configurations.remove(configuration.guildId);
        }

        configurations.put(configuration.guildId, configuration);
    }

    private void SaveConfigs() {
        //Create config directory
        new File("config/servers/").mkdir();

        //Loop through all the configs
        for (GuildConfiguration config : configurations.values()) {
            //Save them all
            try {
                JSONObject json = HashmapToJson(config.settings);

                //I know this isnt ideal, but it works. Prettifys the json form me
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonParser jp = new JsonParser();
                JsonElement je = jp.parse(json.toString());


                FileWriter file = new FileWriter("config/servers/" + config.guildId + ".txt");

                file.write(gson.toJson(je));
                file.flush();
                file.close();

            } catch (IOException e) {
                System.out.println("[ERROR] Could not save config/servers/" + config.guildId);
            }
        }
    }

    private JSONObject HashmapToJson(HashMap hashMap) {
        JSONObject json = new JSONObject();

        for (Object id : hashMap.keySet()) {

            if (id == "") {
                continue;
            }

            String[] parts = ((String) id).split("\\.");

            JSONObject lastObject = json;

            for (int i = 0; i < parts.length - 1; i++) {

                lastObject.put(parts[i], new JSONObject());

                lastObject = lastObject.getJSONObject(parts[i]);
            }

            lastObject.put(parts[parts.length - 1], hashMap.get(id));
        }

        return json;
    }

    private void LoadConfigs() {
        System.out.println("Loading servers config files");

        File serverDir = new File("config/servers/");
        for (File serverConfig : serverDir.listFiles()) {
            String id = serverConfig.getName().split("\\.")[0];
            GuildConfiguration guild = new GuildConfiguration(id);

            try {

                Scanner scanner = new Scanner(serverConfig);

                String config = "";

                while (scanner.hasNext()) {
                    config += scanner.nextLine();
                }

                JSONObject json = new JSONObject(config);

                guild.settings = GetContents(json);

                configurations.put(guild.guildId, guild);

            } catch (FileNotFoundException e) {
                System.out.println("[ERROR] Could not load " + serverConfig.getAbsolutePath());
                return;
            }
        }
    }

    private HashMap<String, Object> GetContents(JSONObject object) {
        HashMap<String, Object> objects = new HashMap();
        for (String obj : object.keySet()) {

            Object value = object.get(obj);

            if (value instanceof JSONObject) {

                HashMap<String, Object> contents = GetContents((JSONObject)value);

                for (Map.Entry set :contents.entrySet()) {
                    objects.put(obj + "." + set.getKey(), set.getValue());
                }
            } else {
                objects.put(obj, value);
            }
        }
        return objects;
    }
}
