package main.java.com.darkzek.SheepBot.Commands;

import main.java.com.darkzek.SheepBot.Enums.TriggerType;
import main.java.com.darkzek.SheepBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Random;

/**
 * Created by darkzek on 28/02/18.
 */
public class Allegedly extends Command {

    public Allegedly() {
        this.description = "DarkZek allegedly made this";
        this.name = "Allegedly";
        this.showInHelp = false;
        this.usage = "Happens approximately once every 200 messages";
        this.trigger = new Trigger(this, TriggerType.MESSAGE_SENT);
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        if (new Random().nextInt(201) != 50) {
            return;
        }
        Reply("***Allegedly***", event);
    }
}
