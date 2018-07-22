package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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

        event.getMessage().delete().queue();

        new Thread(new Runnable() {

            @Override
            public void run() {

                String message = event.getMessage().getContentStripped();

                //Get between all the spaces, to repmove the command bit
                String[] args = message.split(" ");

                if (args.length < 2) {
                    event.processed = true;
                    Reply(Settings.getInstance().prefix + "You forgot to add the message to make big!\nType `>help bigtext` for more information", event);
                    return;
                }

                message = "";

                for (int i = 1; i < args.length; i++) {
                    message += args[i] + " ";
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
                    Reply(Settings.getInstance().prefix + "I'd appreciate it if you'd kindly stop trying to f*** up my program, k thx.", event);
                    return;
                }


                ReplyImage(new ByteArrayInputStream(imageInByte), event);

                event.processed = true;
            }
        }).start();
    }
}
