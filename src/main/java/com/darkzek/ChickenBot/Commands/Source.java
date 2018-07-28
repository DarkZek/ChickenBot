package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class Source extends Command{
    private String link = "https://github.com/DarkZek/ChickenBot";

    public Source() {
        this.description = "Reveals Chicken Bot's source code";
        this.name = "Source";
        this.type = CommandType.ADMINISTRATION;
        this.usage = ">source";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "source");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {
        Reply(Settings.messagePrefix + link, event);
        event.processed = true;
    }
}
