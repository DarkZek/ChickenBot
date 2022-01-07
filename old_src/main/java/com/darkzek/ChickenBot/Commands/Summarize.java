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
import net.dv8tion.jda.api.entities.ChannelType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Summarize extends Command{
    private Summarizer summarizer = new Summarizer();
    private int minSentenceLength = 30;
    private final String configName = "Summarize.enabled";

    String fileName = "SummarizeIgnore.txt";
    String[] blockedDomains;

    public Summarize() {
        this.description = "Toggles tldr; messages";
        this.name = "Summarize";
        this.type = CommandType.INTERNET;
        this.usage = ">summarize to toggle";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.MESSAGE_SENT));
        this.trigger.messageType = MessageType.BOTH;

        manager = GuildConfigurationManager.getInstance();

        LoadBlockedDomains();
    }

    private void LoadBlockedDomains() {
        File file = new File(fileName);
        ArrayList<String> fileList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String token = scanner.nextLine();
                fileList.add(token);
            }

            blockedDomains = fileList.toArray(new String[fileList.size()]);
            return;
        } catch (IOException e) {
        } catch (NoSuchElementException e) {
        }
        System.out.println("[ERROR] Cannot load " + name + "!");
        System.exit(1);
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
        new Thread(() -> {

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
                    Reply(Settings.messagePrefix + "That command is disabled in Direct Messages", event);
                    return;
                }
            }

            Matcher matcher = urlPattern.matcher(event.getMessage().getContentRaw());
            if (matcher.find() == false) {
                return;
            }

            String websiteData;

            try {
                URL link = new URL(event.getMessage().getContentRaw().substring(matcher.start(), matcher.end()));

                for (String blockedDomain : blockedDomains) {
                    if (link.getHost().equalsIgnoreCase(blockedDomain)) {
                        return;
                    }
                }

                URLConnection url = link.openConnection();

                Scanner scanner = new Scanner(url.getInputStream());
                scanner.useDelimiter("\\Z");
                websiteData = scanner.next();

                //Check if its even an article
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

        Reply(Settings.messagePrefix + "Successfully toggled Summarize to `" + newValue + "`", event);
        return;
    }
}
