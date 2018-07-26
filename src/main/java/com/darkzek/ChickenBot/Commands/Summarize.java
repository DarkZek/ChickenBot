package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Configuration.GuildConfiguration;
import com.darkzek.ChickenBot.Configuration.GuildConfigurationManager;
import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import karimo94.Summarizer;
import net.dv8tion.jda.core.entities.ChannelType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Summarize extends Command{
    private Summarizer summarizer = new Summarizer();
    private int minSentenceLength = 45;
    private final String configName = "Summarize.enabled";

    public Summarize() {
        this.description = "Summarize's links in chat (Just chat links)";
        this.name = "Summarize";
        this.type = CommandType.INTERNET;
        this.usage = ">summarize to toggle";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.MESSAGE_SENT));
        this.trigger.messageType = MessageType.BOTH;

        manager = GuildConfigurationManager.getInstance();
    }

    private GuildConfigurationManager manager;


    private final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {

        //Run async

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (event.getChannelType() == ChannelType.TEXT) {
                    GuildConfiguration config = manager.GetGuildConfiguration(event.getGuild().getId() + "");

                    if (event.getCommandName().equalsIgnoreCase("summarize")) {
                        ToggleSummarize(config, event);
                        return;
                    }

                    if (config.Contains(configName) && config.GetBoolean(configName) == false) {
                        //Disabled command!
                        return;
                    }
                } else {
                    if (event.getCommandName().equalsIgnoreCase("summarize")) {
                        event.processed = true;
                        Reply(Settings.getInstance().prefix + "That command is disabled in Direct Messages", event);
                        return;
                    }
                }

                Matcher matcher = urlPattern.matcher(event.getMessage().getContentRaw());
                if (matcher.find() == false) {
                    return;
                }

                //Link found!
                String link = event.getMessage().getContentRaw().substring(matcher.start(), matcher.end());

                // NOTE: Use ArticleExtractor unless DefaultExtractor gives better results for you String
                String websiteData = "";

                try {
                    URLConnection url = new URL(link).openConnection();

                    Scanner scanner = new Scanner(url.getInputStream());
                    scanner.useDelimiter("\\Z");
                    websiteData = scanner.next();

                    if (!websiteData.contains("content=\"article\"")) {
                        return;
                    }

                    websiteData = ArticleExtractor.INSTANCE.getText(websiteData);
                } catch (BoilerpipeProcessingException e) {
                    Reply("ERROR", event);
                    return;
                } catch (MalformedURLException e) {
                    return;
                } catch (IOException e) {
                    return;
                }

                String[] lines = websiteData.split("\n");

                String cleanText = "";

                for (String line : lines ) {

                    if (line.startsWith("FILE PHOTO")) {
                        continue;
                    }
                    if (line.length() < minSentenceLength) {
                        continue;
                    }
                    cleanText += line + "\n" + " ";
                }

                String summary = summarizer.Summarize (cleanText, 3);

                Reply("TLDR: ```" + summary + "```Type >summarize to disable these messages", event);
                event.processed = true;
            }
        }).start();
    }

    private void ToggleSummarize(GuildConfiguration config, CommandRecievedEvent event) {
        //Find the value to change it to
        boolean newValue = false;
        if (config.Contains(configName)) {
            newValue = !config.GetBoolean(configName);
        }

        config.SetObject(configName, newValue);

        config.Apply();

        Reply(Settings.getInstance().prefix + "Successfully toggled Summarize to `" + newValue + "`", event);
        return;
    }
}
