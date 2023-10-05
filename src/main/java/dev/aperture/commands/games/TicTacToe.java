package dev.aperture.commands.games;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicTacToe extends ListenerAdapter
{
    Game game;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (event.getName().equals("tictactoe"))
        {
            User target = event.getOption("user").getAsUser();
            User author = event.getUser();

            if (target.isBot())
            {
                event.reply("You cannot play against bots.").queue();
                return;
            }

            this.game = new Game(target, author, event.getInteraction());
            this.game.run();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        this.game.updateGrid(event);
    }
}
