package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.ChickenBot;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class RemindMe extends Command {

    ArrayList<Reminder> reminders = new ArrayList();

    public RemindMe() {
        this.description = "Reminds you about a post";
        this.name = "RemindMe";
        this.usage = ">remindme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND, TriggerType.BOT_SHUTDOWN), "remindme");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;

        //Load reminders
        LoadRemindMe();

        //Setup repeating task to check if we have any tasks due
        new RemindMeTimer().Setup(this);
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {

        String[] args = event.getArgs();

        if (args.length < 2) {
            Reply(Settings.messagePrefix + "Usage: `>remindme <Number> <Measurement Of Time>`\nExample: `>remindme 1 day`", event);
            return;
        }

        int amountOfTime;

        try {
            amountOfTime = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Reply(Settings.messagePrefix + "Usage: `>remindme <Number> <Measurement Of Time>`\nExample: `>remindme 1 day`", event);
            return;
        }

        long units = stringToTimeunit(args[1]) * amountOfTime;

        if (units == -1) {
            Reply(Settings.messagePrefix + "Please use a correct time unit. I accept `days, hours, minutes and seconds`", event);
            return;
        }

        long time = System.currentTimeMillis();

        if (units > 2678500000L) {
            Reply(Settings.messagePrefix + "That duration is too long! The maximum is 31 days", event);
            return;
        }

        long timeDue = time + units;

        //Create a reminder
        Reminder reminder = new Reminder();
        reminder.timeDue = timeDue;
        reminder.channel = event.getChannel().getId();
        if (event.getGuild() == null) {
            reminder.guild = "@me";
        } else {
            reminder.guild = event.getGuild().getId();
        }
        reminder.message = event.getMessage().getId();
        reminder.userid = event.getAuthor().getId();

        //Add it to the list
        reminders.add(reminder);

        Reply(Settings.messagePrefix + "I will send you a reminder in " + amountOfTime + " " + args[1].toLowerCase() + "!", event);
        event.processed = true;
    }

    public void ShowReminder(Reminder reminder) {
        try {
            String link = "https://discordapp.com/channels/"+ reminder.guild + "/" + reminder.channel + "/" + reminder.message;

            User usr = ChickenBot.jda.getUserById(reminder.userid);

            if (usr == null) {
                //User deleted account
                return;
            }

            PrivateChannel pm = usr.openPrivateChannel().complete();

            pm.sendMessage(new EmbedBuilder()
                    .setTitle("Reminder for " + usr.getName())
                    .setDescription("Reminding you of your \n" + link + "")
                    .setColor(new Color(15064245))
                    .build()).queue();
        } catch (Exception e) {
            //Try report the issue

            StackTraceElement[] stackTrace = e.getStackTrace();

            String trace = "";

            for (StackTraceElement element : stackTrace) {
                trace += element + "\n";
            }

            String msg = "Hey man, we just blew a fuse!" +
                    "\nName: `RemindMe ShowReminder()`" +
                    "\nError: `" + e.getClass().getCanonicalName() + "`" +
                    "\nStacktrace:```" + trace + "```";

            System.out.println(msg);

            if (msg.length() > 2000) {
                msg = msg.substring(0, 1996) + "```...";
            }

            ChickenBot.TellMe(msg);
        }
    }

    public void CheckForCompletedReminders() {
        long time = System.currentTimeMillis();


        for (int i = 0; i < reminders.size(); i++) {
            Reminder reminder = reminders.get(i);
            if (reminder.timeDue < time) {
                //Its due!
                ShowReminder(reminder);
                reminders.remove(i);
            }
        }
    }

    @Override
    public void OnShutdown() {
        if (reminders.size() == 0) {
            //Nothing to save
            return;
        }
        File file = new File("config/reminders.txt");

        try {

            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(reminders);
            oos.flush();
            oos.close();

            System.out.println("Saved Remind Me file");

        } catch (IOException e) {

        }
    }

    public void LoadRemindMe() {
        File file = new File("config/reminders.txt");

        if (!file.exists()) {
            return;
        }

        try {

            ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
            ArrayList<Reminder> readCase = (ArrayList<Reminder>) objectinputstream.readObject();

            reminders = readCase;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Delete the file
        file.delete();
    }

    //Converts units of time into milliseconds
    public long stringToTimeunit(String input) {
        input = input.toLowerCase().trim();

        switch (input) {
            case "month":
            case "months": {
                return 2629800000L;
            }
            case "days":
            case "day":
            case "d":
                return 86400000;
            case "hours":
            case "hour":
            case "h":
                return 3600000;
            case "minute":
            case "minutes":
            case "m":
                return 60000;
            case "seconds":
            case "second":
            case "s":
                return 1000;
        }
        return -1;
    }
}

class Reminder implements Serializable {
    String userid;
    String guild;
    String channel;
    String message;
    long timeDue;
}

class RemindMeTimer extends TimerTask{

    private RemindMe remindMe;

    public void Setup(RemindMe remindMe) {
        this.remindMe = remindMe;

        Timer myTimer = new Timer();

        //Repeat every 10 minutes
        myTimer.schedule(this, 10000, 1000);
    }

    public void run() {
        remindMe.CheckForCompletedReminders();
    }
}