package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Created by darkzek on 31/03/18.
 */
public class Invite extends Command{
    private String inviteLink = "https://discordapp.com/oauth2/authorize?client_id=415740918390456330&scope=bot&permissions=1341643969";

    public Invite() {
        this.description = "Gets an invite link for chicken bot";
        this.name = "Invite";
        this.type = CommandType.ADMINISTRATION;
        this.usage = ">invite";
        this.trigger = new Trigger(this, TriggerType.COMMAND, "invite");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        Reply(Settings.getInstance().prefix + inviteLink, event);
    }
}
