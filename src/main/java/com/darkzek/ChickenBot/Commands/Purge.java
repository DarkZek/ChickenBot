package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Reactions;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by darkzek on 28/02/18.
 */
public class Purge extends Command {


    public Purge() {
        this.description = "Purges messages";
        this.name = "Purge";
        this.type = CommandType.ADMINISTRATION;
        this.usage = ">PURGE <amount>";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "purge");
        this.trigger.SetIgnoreCase(true);
        this.trigger.IncludeBots(true);
        this.trigger.messageType = MessageType.GUILD;
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {
        String[] args = event.getArgs();

        if (args.length != 1) {
            SendMessage(Settings.messagePrefix + "You forgot the amount of messages!", event.getTextChannel());
            return;
        }

        if (event.getGuild() == null) {
            PrivateMessage(Settings.messagePrefix + "What are you doing!", event.getAuthor());
            return;
        }

        if (!event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)) {
            SendMessage(Settings.messagePrefix + "What are you doing!", event.getTextChannel());
            return;
        }

        int num = 0;
        try {
            num = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            SendMessage(Settings.messagePrefix + " You forgot the amount of messages", event.getTextChannel());
            return;
        }

        if (num > 100) {
            Reply(Settings.messagePrefix + "Due to Discord's limitations I cannot purge more than 100 messages at a time", event);
            return;
        }

        if (num < 1) {
            ReplyImage(Reactions.GetRandom(Reactions.getInstance().whatAreYouDoing), event);
            return;
        }

        List<Message> test = event.getTextChannel().getHistory().retrievePast(num).complete();
        try {
            event.getTextChannel().deleteMessages(test).queue();
            return;
        } catch (InsufficientPermissionException e) {
            Reply(Settings.messagePrefix + "I don't have permission! Please add permission `MESSAGE_MANAGE` to use this feature", event);
            return;
        } catch (Exception e) {
            //When command spammed unknown errors occur, so lets ignore them
        }
    }
}
