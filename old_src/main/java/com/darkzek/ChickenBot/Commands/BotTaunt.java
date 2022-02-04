package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Trigger;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BotTaunt extends Command {

    public BotTaunt() {
        this.description = "Taunts the other bots";
        this.name = "Bot Taunt";
        this.usage = "";
        this.showInHelp = false;
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.MESSAGE_SENT));
        this.trigger.messageType = MessageType.GUILD;
        this.trigger.IncludeBots(true);
    }


    public void MessageRecieved(CommandRecievedEvent event) {

        //Once every 200 messages
        if (new Random().nextInt(200) != 51) {
            return;
        }

        //Only respond to bots
        if (!event.getAuthor().isBot()) {
            return;
        }

        //Taunt them
        String[] reactions = new String[] {"\uD83C\uDD71", "\uD83C\uDDE6", "\uD83C\uDDE9", "\uD83C\uDDE7", "\uD83C\uDDF4", "\uD83C\uDDF9"};

        int delay = 0;

        try {
            for (String reaction : reactions) {
                event.getMessage().addReaction(reaction).queueAfter(delay, TimeUnit.MILLISECONDS);
                delay += 250;
            }
        } catch (Exception e) {
        }
    }
}
