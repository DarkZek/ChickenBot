package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.ChickenBot;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RemindMe extends Command {

    ArrayList<Reminder> reminders = new ArrayList();

    public RemindMe() {
        this.description = "Reminds you about a post";
        this.name = "RemindMe";
        this.usage = ">remindme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND, TriggerType.BOT_SHUTDOWN), "remindme");
        this.trigger.SetIgnoreCase(true);

        //Load reminders
        LoadRemindMe();

        //Setup repeating task to check if we have any tasks due
        new RemindMeTimer().Setup(this);
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {

        String[] args = event.getMessage().getContentStripped().split(" ");

        if (args.length < 3) {
            Reply(Settings.getInstance().prefix + "Usage: `>remindme <Number> <Measurement Of Time>`\nExample: `>remindme 1 day`", event);
            return;
        }

        int amountOfTime;

        try {
            amountOfTime = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Reply(Settings.getInstance().prefix + "Usage: `>remindme <Number> <Measurement Of Time>`\nExample: `>remindme 1 day`", event);
            return;
        }

        TimeUnit units = TimeUnit.valueOf((args[2] + "s").toUpperCase());

        long time = System.currentTimeMillis();

        if (units.toMillis(amountOfTime) > 2678500000L) {
            Reply(Settings.getInstance().prefix + "That duration is too long! The maximum is 31 days", event);
            return;
        }

        long timeDue = time + units.toMillis(amountOfTime);

        //Create a reminder
        Reminder reminder = new Reminder();
        reminder.timeDue = timeDue;
        reminder.channel = event.getChannel().getId();
        reminder.guild = event.getGuild().getId();
        reminder.message = event.getMessage().getId();
        reminder.userid = event.getAuthor().getId();

        //Add it to the list
        reminders.add(reminder);

        Reply(Settings.getInstance().prefix + "I will send you a reminder in " + amountOfTime + " " + units.toString().toLowerCase() + "!", event);
    }

    public void ShowReminder(Reminder reminder) {
        String link = "https://discordapp.com/channels/"+ reminder.guild + "/" + reminder.channel + "/" + reminder.message;

        User usr = ChickenBot.jda.getUserById(reminder.userid);

        PrivateChannel pm = usr.openPrivateChannel().complete();

        pm.sendMessage(new EmbedBuilder()
                .setTitle("Reminder for " + usr.getName())
                .setDescription("Reminding you of your [post](" + link + ")")
                .setColor(new Color(15064245))
                .build()).queue();
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