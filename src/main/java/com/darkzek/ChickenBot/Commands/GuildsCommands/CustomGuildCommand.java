package com.darkzek.ChickenBot.Commands.GuildsCommands;

import com.darkzek.ChickenBot.Commands.Command;
import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Guilds.GuildCommand;
import com.darkzek.ChickenBot.Guilds.GuildManager;
import com.darkzek.ChickenBot.Guilds.GuildSettings;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Set;

public class CustomGuildCommand extends Command {

    public CustomGuildCommand() {
        this.description = "Adds a custom command to the guild, which returns a message when ran";
        this.name = "GuildCommand";
        this.type = CommandType.ADMINISTRATION;
        this.usage = ">guildcommand <command_name> <message_to_send>";
        this.trigger = new Trigger(this, TriggerType.COMMAND, "guildcommand");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.GUILD;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentStripped().split(" ");
        if (args.length < 2) {
            Reply(Settings.getInstance().prefix + "Usage: >guildcommand <command>", event);
            return;
        }

        String commandName = args[1].toLowerCase();

        switch (commandName) {
            case "help": {
                Reply(Settings.getInstance().prefix + "Guild command is used for servers to have their own custom commands.\n```" +
                        ">guildcommand add <command_name> <message_to_send>\n" +
                        ">guildcommand remove <command_name>\n" +
                        ">guildcommand list```", event);
                break;
            }
            case "add": {
                AddCommand(event, args);
                break;
            }
            case "remove": {
                RemoveCommand(event, args);
                break;
            }
            case "list": {
                ListCommands(event, args);
                break;
            }
            default: {
                Reply(Settings.getInstance().prefix + "Usage: >guildcommand <command>", event);
                break;
            }
        }
    }

    public void ListCommands(MessageReceivedEvent event, String[] args) {
        GuildSettings guild = GuildManager.getInstance().GetGuildSettings(event.getTextChannel().getId());

        GuildCommand[] commands = guild.commands;

        String msg = "Currently this guild has `" + guild.commands.length+ "` commands\n```";

        for (com.darkzek.ChickenBot.Guilds.GuildCommand command : commands) {
            msg += command.activator + " | " + command.result + "\n";
        }
        msg += "```";

        Reply(msg , event);
    }

    public void RemoveCommand(MessageReceivedEvent event, String[] args) {
        if (args.length != 3) {
            Reply(Settings.getInstance().prefix + "Usage: >guildcommand remove <command_name>", event);
            return;
        }
        if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Reply(Settings.getInstance().prefix + "You need permission MANAGE_CHANNEL to do that!", event);
            return;
        }

        String commandName = args[2];

        GuildSettings guild = GuildManager.getInstance().GetGuildSettings(event.getTextChannel().getId());

        guild.RemoveCommand(commandName);

        GuildManager.getInstance().SetGuild(guild);

        GuildCommand[] commands = guild.commands;

        String msg = "Currently this guild has `" + guild.commands.length+ "` commands```";

        for (com.darkzek.ChickenBot.Guilds.GuildCommand command : commands) {
            msg += command.activator + " | " + command.result + "\n";
        }
        msg += "```";

        Reply(msg , event);
    }

    public void AddCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 3) {
            Reply(Settings.getInstance().prefix + "Usage: >guildcommand add <command_name> <message_to_send>\nNote: You can not have spaces in the command name", event);
            return;
        }
        if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Reply(Settings.getInstance().prefix + "You need permission MANAGE_CHANNEL to do that!", event);
            return;
        }

        String commandName = args[2];

        String message = "";

        for (int i = 3; i < args.length; i++) {
            message += args[i] + " ";
        }

        GuildSettings guild = GuildManager.getInstance().GetGuildSettings(event.getTextChannel().getId());

        if (guild.HasCommand(commandName)) {
            //Server already has command
            Reply(Settings.getInstance().prefix + "You already have a command named `" + commandName + "`!", event);
            return;
        }

        guild.AddCommand(new GuildCommand(commandName, message));

        GuildManager.getInstance().SetGuild(guild);

        Reply("Currently this guild has `" + guild.commands.length+ "` commands" , event);

        GuildCommand[] commands = guild.commands;

        String msg = "```";

        for (com.darkzek.ChickenBot.Guilds.GuildCommand command : commands) {
            msg += command.activator + " | " + command.result + "\n";
        }
        msg += "```";

        Reply(msg , event);
    }
}
