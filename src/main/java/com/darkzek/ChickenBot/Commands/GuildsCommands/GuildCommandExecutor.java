package com.darkzek.ChickenBot.Commands.GuildsCommands;

import com.darkzek.ChickenBot.Commands.Command;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Guilds.GuildCommand;
import com.darkzek.ChickenBot.Guilds.GuildManager;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GuildCommandExecutor extends Command {

    private GuildManager manager;

    public GuildCommandExecutor() {
        this.showInHelp = false;
        this.trigger = new Trigger(this, TriggerType.MESSAGE_SENT);
        this.trigger.messageType = MessageType.GUILD;

        manager = GuildManager.getInstance();
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {

        String message = event.getMessage().getContentStripped();
        String m = message;

        //Check if it uses  the command prefix or tags us
        if (!event.getMessage().isMentioned(event.getJDA().getSelfUser(), net.dv8tion.jda.core.entities.Message.MentionType.USER)) {
            //Check if the message contains the correct phrase
            if (!message.startsWith(Settings.getInstance().enabler)) {
                return;
            }
            m = m.substring(1);
        } else {
            int index = m.indexOf(' ') + 1;
            if (index == -1) {
                return;
            }
            m = m.substring(index);
        }

        String guildId = event.getTextChannel().getId();

        //Check if the guild has made custom commands
        if (!manager.GuildHasCustomCommands(guildId)) {
            return;
        }


        GuildCommand[] guildCommands = manager.GetGuildSettings(guildId).commands;

        for(GuildCommand command : guildCommands) {
            if (m.startsWith(command.activator)) {
                Reply(command.result, event);
                return;
            }
        }

    }
}
