package dev.aperture.commands.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import dev.aperture.Constants;

public class Game
{
    private Player _player1, _player2, _currentPlayer;
    private Optional<Player> _winner;
    private EmbedBuilder _embed;
    private SlashCommandInteraction _interaction;
    private ActionRow _row1, _row2, _row3, _row4;

    private int[] _board;

    public Game(User user1, User user2, SlashCommandInteraction interaction)
    {
        this._player1 = new Player(user1, '❌');
        this._player2 = new Player(user2, '⭕');

        this._interaction = interaction;

        this._currentPlayer = Math.random() < 0.5 ? this._player1 : this._player2;
        this._winner = Optional.empty();

        // Fill the board with empty data.

        this._board = new int[9];
        for (int i = 0; i < 9; i++)
        {
            this._board[i] = 0;
        }

        this._embed = new EmbedBuilder()
                .setTitle("⚔️ " + this._player1.user.getName() + " vs " + this._player2.user.getName())
                .setDescription(this._currentPlayer.sign + " " + this._currentPlayer.user.getName() + " Your turn!")
                .setColor(Constants.GENERAL);

        this._row1 = ActionRow.of(Button.secondary("primary0", "‎"), Button.secondary("primary1", "‎"),
                Button.secondary("primary2", "‎"));

        this._row2 = ActionRow.of(Button.secondary("primary3", "‎"), Button.secondary("primary4", "‎"),
                Button.secondary("primary5", "‎"));

        this._row3 = ActionRow.of(Button.secondary("primary6", "‎"), Button.secondary("primary7", "‎"),
                Button.secondary("primary8", "‎"));

        this._row4 = ActionRow.of(Button.danger("end", "End Game"));
    }

    public void run()
    {
        this._interaction.replyEmbeds(this._embed.build()).addComponents(this._row1, this._row2, this._row3, this._row4)
                .queue();
    }

    public void updateGrid(ButtonInteractionEvent event)
    {
        ButtonInteraction interaction = event.getInteraction();
        Button b = interaction.getButton();

        // Check if the end game button has been clicked
        if (b.getId().equals("end"))
        {
            if (!(interaction.getUser().getId().equals(this._player1.user.getId())
                    || interaction.getUser().getId().equals(this._player2.user.getId())))
            {
                event.reply("Get lost idiot. You are not even playing.").setEphemeral(true).queue();
                return;
            }

            this._interaction.getHook().editOriginal("Game has been ended").setActionRow().setEmbeds().queue();
            ;
            return;
        }

        int position = Integer.parseInt(b.getId().substring(b.getId().length() - 1));

        // Check if the move is valid
        if (!this.isMoveValid(position))
        {
            this._interaction.reply("Your move is invalid! This cell is already occupied. Try with another move.")
                    .setEphemeral(true).queue();

            return;
        }

        // Update the board
        List<Button> buttons = new ArrayList<>();

        if (position <= 2)
        {
            buttons = new ArrayList<>(this._row1.getButtons());
            buttons.set(position % 3, Button.secondary(b.getId(), String.valueOf(this._currentPlayer.sign)));

            this._row1 = ActionRow.of(buttons);
        }
        else if (position <= 5)
        {
            buttons = new ArrayList<>(this._row2.getButtons());
            buttons.set(position % 3, Button.secondary(b.getId(), String.valueOf(this._currentPlayer.sign)));

            this._row2 = ActionRow.of(buttons);
        }
        else
        {
            buttons = new ArrayList<>(this._row3.getButtons());
            buttons.set(position % 3, Button.secondary(b.getId(), String.valueOf(this._currentPlayer.sign)));

            this._row3 = ActionRow.of(buttons);
        }

        // Set the current player to the next player
        this._currentPlayer = this.nextPlayer(this._currentPlayer);

        this._embed.setDescription(this._currentPlayer.sign + " " + this._currentPlayer.user.getName() + " Your turn!");
        this._interaction.getHook().editOriginalEmbeds(this._embed.build()).queue();
        event.editComponents(this._row1, this._row2, this._row3, this._row4).queue();
    }

    private boolean isMoveValid(int position)
    {
        return this._board[position] == 0 ? true : false;
    };

    private Player nextPlayer(Player currentPlayer)
    {
        if (currentPlayer == this._player1)
            return this._player2;
        else
            return this._player1;
    };
}

class Player
{
    User user;
    char sign;

    Player(User user, char sign)
    {
        this.user = user;
        this.sign = sign;
    }
}
