package com.darkzek.ChickenBot;

import com.darkzek.ChickenBot.Commands.Command;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.List;

/**
 * Created by darkzek on 28/02/18.
 */
public class Trigger {
    List<TriggerType> type;
    String arg;
    Command command;
    boolean includeBots = false;
    boolean ignoreCase = true;
    public MessageType messageType = MessageType.GUILD;

    public Trigger(Command command, List<TriggerType> type) {
        this.type = type ;
        this.command = command;
        Setup();
    }

    public Trigger(Command command, List<TriggerType> type, String argument) {
        this.type = type;
        arg = argument;
        this.command = command;
        Setup();
    }

    public void IncludeBots(boolean includeBots) {
        this.includeBots = includeBots;
    }

    public void SetIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        if (ignoreCase) {
            arg = arg.toLowerCase();
        }
    }

    public void MessageRecieved(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if (!includeBots && event.getAuthor().isBot()) {
            return;
        }

        if (type.contains(TriggerType.MESSAGE_SENT_CONTAINS)) {
            //Check if the message contains the correct phrase
            if (!message.contains(arg)) {
                return;
            }
        }

        MessageType msgType;
        if (event.getChannelType().isGuild()) {
            msgType = MessageType.GUILD;
        } else {
            msgType = MessageType.PRIVATE;
        }

        if (ignoreCase) {
            message = message.toLowerCase();
        }


        if (type.contains(TriggerType.COMMAND)) {
            //Get message content
            String m = event.getMessage().getContentStripped();
            //Check if it uses  the command prefix or tags us
            if (!event.getMessage().isMentioned(event.getJDA().getSelfUser(), net.dv8tion.jda.core.entities.Message.MentionType.USER)) {
                //Check if the message contains the correct phrase
                if (!message.startsWith(Settings.getInstance().enabler)) {
                    return;
                }
                m = m.substring(1);
            } else {
                int index = m.indexOf(' ') + 1;
                if (index == -1) {
                    return;
                }
                m = m.substring(index);
            }
            if (ignoreCase) {
                m = m.toLowerCase();
            }
            if (!m.startsWith(arg)) {
                return;
            }
        }

        //Check if the command should even run for this type of message
        if (this.messageType != MessageType.BOTH && this.messageType != msgType) {
            return;
        }

        //Check if the command should even run for this type of message
        if (this.messageType != MessageType.BOTH && this.messageType != msgType) {
            command.Reply(Settings.getInstance().prefix + "You cant use this command in this channel type!", event);
            return;
        }

        command.MessageRecieved(event);
    }

    public void Shutdown() {
        command.OnShutdown();
    }

    public void MessageDeleted(MessageDeleteEvent event) {
        command.MessageDeleted(event);
    }

    public void Setup() {
        //Let the command manager know what we're listening for

        for (TriggerType tType : type) {
            switch (tType) {
                case MESSAGE_SENT: {
                    CommandManager.getInstance().messageTriggers.add(this);
                    break;
                }
                case MESSAGE_SENT_CONTAINS: {
                    CommandManager.getInstance().messageTriggers.add(this);
                    break;
                }
                case COMMAND: {
                    CommandManager.getInstance().messageTriggers.add(this);
                    break;
                }
                case MESSAGE_DELETED: {
                    CommandManager.getInstance().deletedTriggers.add(this);
                    break;
                }
                case BOT_SHUTDOWN: {
                    CommandManager.getInstance().shutdownTriggers.add(this);
                    break;
                }
            }
        }
    }
}
