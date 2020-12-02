package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.exceptions.PermissionException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BigText extends Command {

    public BigText() {
        this.description = "Converts text into a big image to prove your point";
        this.name = "BigText";
        this.usage = ">bigtext <input words>";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "bigtext");
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {

        if (event.getChannelType() == ChannelType.TEXT) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException e) { }
        }

        new Thread(() -> {

            //Get between all the spaces, to remove the command bit

            if (event.getArgs().length < 2) {
                event.processed = true;
                Reply(Settings.messagePrefix + "You forgot to add the message to make big!\nType `>help bigtext` for more information", event);
                return;
            }

            String message = "";

            for (int i = 1; i < event.getArgs().length; i++) {
                message += event.getArgs()[i] + " ";
            }

            float fontSize = 800 - (message.length() * 10);

            if (fontSize < 200) {
                fontSize = 200;
            }

            final BufferedImage image = new BufferedImage(4560, 1500,
                    BufferedImage.TRANSLUCENT);


            Graphics g = image.getGraphics();
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(fontSize));
            g.drawString(message, 10, 800);
            g.dispose();


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] imageInByte;

            try {
                ImageIO.write( image, "png", baos );

                baos.flush();
                imageInByte = baos.toByteArray();
                baos.close();

            } catch (IOException e) {
                event.processed = true;
                Reply(Settings.messagePrefix+ "I'd appreciate it if you'd kindly stop trying to f*** up my program, k thx.", event);
                return;
            }


            ReplyImage(new ByteArrayInputStream(imageInByte), event);

            event.processed = true;
        }).start();
    }
}
