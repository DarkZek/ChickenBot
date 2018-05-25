package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by darkzek on 1/04/18.
 */
public class Raffle extends Command {

    public Raffle() {
        this.description = "Gets a random user from a channel";
        this.name = "Raffle";
        this.usage = ">raffle";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "raffle");
        this.trigger.SetIgnoreCase(true);
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        //Get all members in channel
        List<Member> members = event.getTextChannel().getMembers();

        //Get random member
        Member member = members.get(new Random().nextInt(members.size() - 1));

        Reply(Settings.getInstance().prefix + "Randomly selected " + member.getAsMention() + "!", event);
    }
}
