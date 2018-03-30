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
public class Help extends Command {

    public Help() {
        this.description = "Lists all available commands";
        this.name = "Help";
        this.type = CommandType.ADMINISTRATION;
        this.usage = ">help <command>";
        this.trigger = new Trigger(this, TriggerType.COMMAND, "help");
        this.trigger.SetIgnoreCase(true);
        this.trigger.IncludeBots(true);
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");

        String message = Settings.prefix + "```";

        if (args.length > 1) {
            //User wants to get help about a specific command
            boolean foundCommand = false;
            String search = args[1];
            if (args.length > 2) {
                search = args[2];
            }
            for (Command cmd : CommandLoader.commands) {
                if (cmd.name.equalsIgnoreCase(search)) {
                    //This is the command
                    message += "\n" + cmd.name + "\n" + cmd.description + "\nUsage: " + cmd.usage;
                    foundCommand = true;
                    break;
                }
            }
            if (!foundCommand) {
                message += "Error: Command not found";
            }
        } else {
            for (Command cmd : CommandLoader.commands) {
                if (cmd.showInHelp) {
                    message += ">" + cmd.name + " - " + cmd.description + "\n";
                }
            }
        }
        message += "```";

        Reply(message, event, true);
    }
}