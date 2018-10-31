package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Commands.GuildsCommands.CustomGuildCommand;
import com.darkzek.ChickenBot.Commands.GuildsCommands.GuildCommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darkzek on 28/02/18.
 */
public class CommandLoader {

    public static List<Command> commands = new ArrayList<>();

    public static void Load() {
        commands.add(new Help());
        commands.add(new Purge());
        commands.add(new LMGTFY());
        commands.add(new Search());
        commands.add(new Dankmeme());
        commands.add(new DankMemeEmotes());
        commands.add(new DONG());
        commands.add(new Invite());
        commands.add(new CustomGuildCommand());
        commands.add(new GuildCommandExecutor());
        commands.add(new Source());
        commands.add(new BigText());
        commands.add(new Emote());
        commands.add(new Chat());
        commands.add(new DistanceConversion());
        commands.add(new RemindMe());
        commands.add(new BotTaunt());
        commands.add(new Summarize());
        commands.add(new ChangeLog());

        System.out.println("Successfully loaded all Commands");
    }
}
