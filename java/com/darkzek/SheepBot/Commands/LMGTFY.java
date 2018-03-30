package main.java.com.darkzek.SheepBot.Commands;

import main.java.com.darkzek.SheepBot.Enums.CommandType;
import main.java.com.darkzek.SheepBot.Enums.MessageType;
import main.java.com.darkzek.SheepBot.Enums.TriggerType;
import main.java.com.darkzek.SheepBot.Settings;
import main.java.com.darkzek.SheepBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Created by darkzek on 28/02/18.
 */
public class LMGTFY extends Command {

    public LMGTFY() {
        this.description = "Let me google that for you generator";
        this.name = "LMGTFY";
        this.type = CommandType.INTERNET;
        this.usage = ">LMGTFY <search_term>";
        this.trigger = new Trigger(this, TriggerType.COMMAND, "lmgtfy");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().length() < 9) {
            Reply(Settings.prefix + "You need to supply a quote to google!", event);
            return;
        }

        String msg = event.getMessage().getContentRaw().substring(8);

        String message = "<https://lmgtfy.com/?q=" + msg.replaceAll(" ", "+") + ">";
        Reply(message, event, true);
    }
}
