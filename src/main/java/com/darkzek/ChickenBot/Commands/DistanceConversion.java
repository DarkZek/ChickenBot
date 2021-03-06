package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Configuration.GuildConfiguration;
import com.darkzek.ChickenBot.Configuration.GuildConfigurationManager;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.*;
import java.util.Arrays;

public class DistanceConversion extends Command {
    public DistanceConversion() {
        this.description = "Converts different measurement types";
        this.name = "DistanceConversion";
        this.showInHelp = false;
        this.usage = "Say a unit of measurement and it will convert it";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.MESSAGE_SENT, TriggerType.BOT_SHUTDOWN));
        this.trigger.messageType = MessageType.BOTH;

        LoadMeasurements();
        manager = GuildConfigurationManager.getInstance();
    }

    private GuildConfigurationManager manager;

    private final String configName = "DistanceConversion.enabled";

    //Listing all the variables - in how many Metres they are
    //Metric
    float[] conversions = new float[8];

    float metersToBananas = 0.2032f;

    String[][] activators = new String[8][0];

    private void LoadMeasurements() {
        //mm
        conversions[0] = 0.001f;
        //cm
        conversions[1] = 0.01f;
        //m
        conversions[2] = 1;
        //km
        conversions[3] = 1000f;

        //Imperial
        //inches
        conversions[4] = 0.0254f;
        //feet
        conversions[5] = 0.3048f;
        //yards
        conversions[6] = 0.9144f;
        //miles
        conversions[7] = 1609.344f;

        activators[0] = new String[] {"millimeters", "mm"};
        activators[1] = new String[] {"centimeters", "cm"};
        activators[2] = new String[] {"meters"};
        activators[3] = new String[] {"kilometers", "km"};

        activators[4] = new String[] {"inches", "\"", "inch"};
        activators[5] = new String[] {"feet", "foot"};
        activators[6] = new String[] {"yards", "yard"};
        activators[7] = new String[] {"miles", "mile"};
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {

        if (event.getChannelType() != ChannelType.PRIVATE) {
            GuildConfiguration config = manager.GetGuildConfiguration(event.getGuild().getId() + "");

            //Toggles distance conversion
            if (event.getMessage().getContentRaw().toLowerCase() .startsWith(">distanceconversion")) {
                ToggleDistanceConversion(config, event);
            }

            if (config.Contains(configName)) {
                if (!config.GetBoolean(configName)) {
                    //Disabled!
                    return;
                }
            }
        }

        ConvertedNumber response = FindMeasurement(event.getMessage().getContentDisplay());

        if (response == null) {
            //No measurement to convert
            return;
        }

        //Generate embed
        Reply(new EmbedBuilder()
                .setColor(new Color(16138809))
                .addField(response.originalNumber + " " + response.originalMeasurement + " is ", "`" + response.convertedNumber + " " + response.newMeasurement + "`", false)
                .addField("or", "`" + String.valueOf(response.bananas) + " bananas`", false)
                .setFooter("Admins: Use the command >DistanceConversion to disable this feature", null)
                .build(), event);

        event.processed = true;
    }

    private void ToggleDistanceConversion(GuildConfiguration config, CommandRecievedEvent event) {
        //Find the value to change it to
        boolean newValue = false;
        if (config.Contains(configName)) {
            newValue = !config.GetBoolean(configName);
        }

        config.SetObject(configName, newValue);

        config.Apply();

        Reply(Settings.messagePrefix + "Successfully toggled Measurement Conversion to `" + newValue + "`", event);
        return;
    }

    private ConvertedNumber FindMeasurement(String message) {

        int measurementType = -1;
        int measurementIndex = -1;

        loop:
        for (int i = 0; i < activators.length; i++) {
            for ( String activator :activators[i]) {
                if (message.contains(activator)) {
                    //We've got a match!
                    measurementType = i;
                    measurementIndex = message.indexOf(activator);
                    break loop;
                }
            }
        }

        //No measurement was found
        if (measurementType == -1) {
            return null;
        }

        String measurement = message.substring(0, measurementIndex).trim();

        //Check for the numbers
        int amountOfNumbers = 0;

        if (measurement.length() < 1) {
            return null;
        }

        //Get how many numbers infront of it are numeric
        while (isNumeric(measurement.charAt(measurement.length() - 1 - amountOfNumbers))) {
            amountOfNumbers++;
            if (amountOfNumbers == measurement.length()) {
                break;
            }
        }

        //Get the number to convert
        measurement = measurement.substring(measurement.length() - amountOfNumbers);

        float number = 0;
        try {
            number = Integer.parseInt(measurement);
        } catch (NumberFormatException e) {
            return null;
        }

        //Convert it!
        float meters = number * conversions[measurementType];

        int numberToConvertTo = measurementType;

        if (numberToConvertTo >= 4) {
            numberToConvertTo = numberToConvertTo - 4;
        } else {
            numberToConvertTo = numberToConvertTo + 4;
        }

        float converted = meters / conversions[numberToConvertTo];
        double bananas = meters / metersToBananas;

        ConvertedNumber convertedNumber = new ConvertedNumber();
        convertedNumber.convertedNumber = converted;
        convertedNumber.bananas = bananas;
        convertedNumber.newMeasurement = activators[numberToConvertTo][0];
        convertedNumber.originalMeasurement = activators[measurementType][0];
        convertedNumber.originalNumber = number;

        return convertedNumber;
    }

    private boolean isNumeric(Character c) {
        if (c == '0' || c == '1' ||c == '2' ||c == '3' ||c == '4' ||c == '5' ||c == '6' ||c == '7' ||c == '8' ||c == '9' ||c == '.') {
            return true;
        }
        return false;
    }


}

class ConvertedNumber {
    public double bananas;
    public float convertedNumber;
    public float originalNumber;
    public String originalMeasurement;
    public String newMeasurement;
}
