package com.darkzek.ChickenBot.Guilds;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GuildManager {

    GuildSettings[] guilds;

    private static GuildManager guildManager = new GuildManager( );

    private GuildManager() {LoadGuilds();SetupShutdownHook();}

    /* Static 'instance' method */
    public static GuildManager getInstance( ) {
        return guildManager;
    }

    private void SetupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SaveGuilds();
        }));
    }

    private void LoadGuilds() {
        System.out.println("Loading servers config...");

        List<GuildSettings> guilds = new ArrayList();

        String settings = "";

        try {
            File file = new File("config/servers.json");

            Scanner scanner = new Scanner(file);

            //Make sure it's not empty
            if (!scanner.hasNext()) {
                return;
            }

            settings = scanner.nextLine();

        } catch (IOException e) {
            System.out.println("[ERROR] Could not read servers config file " + e.fillInStackTrace());
            return;
        }

        JSONParser jsonParser = new JSONParser();

        JSONObject settingsData;
        try {
            settingsData = (JSONObject) jsonParser.parse(settings);
        } catch (ParseException e) {
            System.out.println("[ERROR] Could not parse servers config file " + e.fillInStackTrace());
            System.exit(0);
            return;
        }
        if (settingsData == null) {
            System.out.println("[ERROR] Could not parse servers config file");
            System.exit(0);
            return;
        }

        JSONArray servers = (JSONArray) settingsData.get("servers");

        for (int i = 0; i < servers.size(); i++) {

            JSONObject server = (JSONObject) servers.get(i);

            String serverId = (String)server.get("id");

            JSONArray commands = (JSONArray) server.get("commands");

            List<GuildCommand> commandsList = new ArrayList();

            for (int c = 0; c < commands.size(); c++) {
                JSONObject command = (JSONObject) commands.get(c);

                String commandName = command.keySet().toString();
                commandName = commandName.substring(1, commandName.length() - 1);
                String message = (String)command.get(commandName);

                GuildCommand guildCommand = new GuildCommand(commandName, message);
                commandsList.add(guildCommand);
            }

            GuildSettings guild = new GuildSettings(serverId, commandsList.toArray(new GuildCommand[commandsList.size()]));

            guilds.add(guild);
        }

        this.guilds = guilds.toArray(new GuildSettings[guilds.size()]);
        System.out.println("Successfully loaded servers config!");
    }

    private void SaveGuilds() {
        //Serialize all guild fields
        JSONObject jsonObject = new JSONObject();

        JSONArray serversList = new JSONArray();

        for (int i = 0; i < guilds.length; i++) {
            GuildSettings guild = guilds[i];

            String id = guild.GetGuildId();

            JSONArray commandsJSON = new JSONArray();

            GuildCommand[] commandsArray = guild.commands;

            //Save commands
            for(int c = 0; c < commandsArray.length; c++) {
                GuildCommand command = commandsArray[c];

                JSONObject commandObject = new JSONObject();
                commandObject.put(command.activator, command.result);

                commandsJSON.add(commandObject);
            }

            JSONObject server = new JSONObject();
            server.put("commands", commandsJSON);
            server.put("id", id);

            serversList.add(server);
        }

        jsonObject.put("servers", serversList);

        //Write to file
        try {
            File configFolder = new File("config/");
            configFolder.mkdir();

            FileWriter file = new FileWriter("config/servers.json");
            file.write(jsonObject.toJSONString());
            file.flush();
            file.close();

        } catch (IOException e) {
            System.out.println("[ERROR] Could not write out servers config file " + e.fillInStackTrace());
            return;
        }
        System.out.println("Successfully saved servers config file");

    }

    public boolean GuildHasCustomCommands(String guildId) {
        for (GuildSettings guild : guilds) {
            if (guild.GetGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }

    public GuildSettings GetGuildSettings(String guildId) {
        for (GuildSettings guild : guilds) {
            if (guild.GetGuildId().equals(guildId)) {
                return guild;
            }
        }
        //Add a new guild
        GuildSettings newGuild = new GuildSettings(guildId, new GuildCommand[0]);
        AddGuild(newGuild);
        return newGuild;
    }

    public void SetGuild(GuildSettings guild) {
        String guildId = guild.GetGuildId();
        //Compare the ID's
        for (int i = 0; i < guilds.length; i++) {
            GuildSettings compareGuild = guilds[i];
            if (compareGuild.GetGuildId().equals(guildId)) {
                guilds[i] = guild;
                return;
            }
        }
        AddGuild(guild);
    }

    private void AddGuild(GuildSettings guild) {
        //Arrays.asList creates a fixed size list, so you can add and remove which is why linkedlist is needed
        List<GuildSettings> guildsList = new LinkedList<>(Arrays.asList(guilds));
        guildsList.add(guild);
        guilds = guildsList.toArray(new GuildSettings[guildsList.size()]);
    }


}
