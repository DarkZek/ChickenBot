package main.java.com.darkzek.SheepBot.Commands;

import main.java.com.darkzek.SheepBot.Enums.CommandType;
import main.java.com.darkzek.SheepBot.Enums.MessageType;
import main.java.com.darkzek.SheepBot.Enums.TriggerType;
import main.java.com.darkzek.SheepBot.Settings;
import main.java.com.darkzek.SheepBot.Trigger;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
        this.trigger = new Trigger(this, TriggerType.COMMAND, "purge");
        this.trigger.SetIgnoreCase(true);
        this.trigger.IncludeBots(true);
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            SendMessage(Settings.prefix + "What are you doing!", event.getTextChannel());
            return;
        }

        if (event.getGuild() == null) {
            PrivateMessage(Settings.prefix + "What are you doing!", event.getAuthor());
            return;
        }

        if (!event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)) {
            SendMessage(Settings.prefix + "What are you doing!", event.getTextChannel());
            return;
        }

        int num = 0;
        try {
            num = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            SendMessage(Settings.prefix + " You forgot the amount of messages", event.getTextChannel());
            return;
        }

        List<Message> test = event.getTextChannel().getHistory().retrievePast(num).complete();
        event.getTextChannel().deleteMessages(test).queue();
    }
}
