package com.darkzek.ChickenBot.Configuration;

import java.util.HashMap;

public class GuildConfiguration {

    String guildId;

    protected HashMap<String, Object> settings = new HashMap();

    protected GuildConfiguration(String guildId) {
        this.guildId = guildId;
    }

    public Object GetObject(String identifier) {
        if (settings.containsKey(identifier)) {
            return settings.get(identifier);
        }
        return null;
    }

    public void SetObject(String identifier, Object data) {

        if (settings.containsKey(identifier)) {
            settings.remove(identifier);
        }

        settings.put(identifier, data + "");
    }

    public String GetString(String identifier) {
        if (!settings.containsKey(identifier)) {
            return null;
        }

        //Config contains it
        Object data = settings.get(identifier);

        //Make sure its a string
        if (data instanceof String) {
            return (String)data;
        }
        return null;
    }

    public int GetInt(String identifier) {
        if (!settings.containsKey(identifier)) {
            return -1;
        }

        //Config contains it
        int data = Integer.parseInt((String)settings.get(identifier));

        return data;
    }

    public Boolean GetBoolean(String identifier) {
        if (!settings.containsKey(identifier)) {
            return null;
        }

        //Config contains it
        boolean data = Boolean.parseBoolean((String) settings.get(identifier));

        return data;
    }

    public Boolean Contains(String identifier) {

        return settings.containsKey(identifier);
    }

    public void Apply() {
        GuildConfigurationManager.getInstance().SetGuildConfiguration(this);
    }

}
