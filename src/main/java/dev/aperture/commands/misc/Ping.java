package dev.aperture.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Ping extends ListenerAdapter
{
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (event.getName().equals("ping"))
        {
            long time = System.currentTimeMillis();

            event.reply("Pinging...")
                    .flatMap(v -> event.getHook().editOriginalFormat("%d ms", System.currentTimeMillis() - time))
                    .queue();
        }
    }
}
