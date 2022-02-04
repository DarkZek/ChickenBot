package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.*;
import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Arrays;

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
        if (event.getUser().isBot()) {
            return;
        }

        //Check the user has permission to do that in this channel
        if (!event.getMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
            return;
        }


        User messageAuthor = event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor();

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

        if (event.getChannelType() == ChannelType.PRIVATE) {
            Dankmeme.DirectSendMeme(post, event.getUser());
        } else {
            Dankmeme.SendMeme(post, event.getTextChannel());
        }

    }
}