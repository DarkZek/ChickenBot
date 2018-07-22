package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
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
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.MESSAGE_SENT));
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {

        if (new Random().nextInt(4001) != 50) {
            return;
        }
        Reply("***Allegedly***", event);
    }
}
