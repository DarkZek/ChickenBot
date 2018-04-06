package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Source extends Command{
    private String link = "https://github.com/DarkZek/ChickenBot";

    public Source() {
        this.description = "Reveals Chicken Bot's source code";
        this.name = "Source";
        this.type = CommandType.ADMINISTRATION;
        this.usage = ">source";
        this.trigger = new Trigger(this, TriggerType.COMMAND, "source");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        Reply(Settings.getInstance().prefix + link, event);
    }
}
