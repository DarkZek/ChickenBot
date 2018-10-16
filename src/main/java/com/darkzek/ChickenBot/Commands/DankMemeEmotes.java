package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.*;
import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by darkzek on 28/02/18.
 */
public class DankMemeEmotes extends Command {

    public DankMemeEmotes() {
        this.description = "Adds the emote for meme functionality";
        this.name = "DankMemeEmotes";
        this.type = CommandType.FUN;
        this.showInHelp = false;
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.EMOTE_ADDED), ":call_me:");
        this.trigger.IncludeBots(true);
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void EmoteAdded(MessageReactionAddEvent event) {

        //Make sure the emote wasn't by chicken bot
        if (event.getUser().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            return;
        }


        User messageAuthor = event.getChannel().getMessageById(event.getMessageId()).complete().getAuthor();

        //Check if emote is to a chicken bot message
        if (!messageAuthor.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            return;
        }

        //Show!
        RedditPost post;

        while (true) {
            post = Dankmeme.getMeme();
            if (post != null) {
                break;
            }
        }

        Dankmeme.SendMeme(post, event.getTextChannel());

    }
}