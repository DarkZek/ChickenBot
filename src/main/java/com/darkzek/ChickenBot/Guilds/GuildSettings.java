package com.darkzek.ChickenBot.Guilds;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GuildSettings {
    public GuildCommand[] commands;

    private String guildId;

    public GuildSettings(String guildId, GuildCommand[] commands) {
        this.guildId = guildId;
        this.commands = commands;
    }

    public void AddCommand(GuildCommand command) {
        List<GuildCommand> guildsList = new LinkedList<>(Arrays.asList(commands));
        guildsList.add(command);
        commands = guildsList.toArray(new GuildCommand[guildsList.size()]);
    }

    public boolean HasCommand(String commandName) {
        commandName = commandName.toLowerCase();

        List<GuildCommand> guildsList = new LinkedList<>(Arrays.asList(commands));

        for (int i = 0; i < guildsList.size(); i++) {
            String name = guildsList.get(i).activator;
            if (name.toLowerCase().equals(commandName)) {
                return true;
            }
        }
        return false;
    }

    public void RemoveCommand(String commandName) {
        commandName = commandName.toLowerCase();

        List<GuildCommand> guildsList = new LinkedList<>(Arrays.asList(commands));

        for (int i = 0; i < guildsList.size(); i++) {
            String name = guildsList.get(i).activator;
            if (name.toLowerCase().equals(commandName)) {
                guildsList.remove(i);
                break;
            }
        }

        commands = guildsList.toArray(new GuildCommand[guildsList.size()]);
    }

    public String GetGuildId() {
        return guildId;
    }
}
