package dev.aperture.commands.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.aperture.Constants;

public class Game
{
    private Player _player1, _player2, _currentPlayer;
    private char _winner;
    private EmbedBuilder _embed;
    private SlashCommandInteraction _interaction;
    private ActionRow _row1, _row2, _row3, _row4;

    private char[] _board;

    public Game(User user1, User user2, SlashCommandInteraction interaction)
    {
        this._player1 = new Player(user1, '❌');
        this._player2 = new Player(user2, '⭕');

        this._interaction = interaction;

        this._currentPlayer = Math.random() < 0.5 ? this._player1 : this._player2;
        this._winner = ' ';

        // Fill the board with empty data.
        this._board = new char[9];
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
                Message msg = this._interaction.getChannel()
                        .sendMessage("Get lost " + interaction.getUser().getAsMention() + " You are not even playing.")
                        .complete();

                msg.delete().queueAfter(2, TimeUnit.SECONDS);
                return;
            }

            this._interaction.getHook().deleteOriginal().queue();
            this._interaction.getMessageChannel().sendMessage("Game ended.").queue();
            return;
        }

        // Check if the move is made by current user
        if (!interaction.getUser().getId().equals(this._currentPlayer.user.getId()))
        {
            Message msg = interaction.getChannel()
                    .sendMessage("Its not your turn. " + interaction.getUser().getAsMention()).complete();

            msg.delete().queueAfter(2, TimeUnit.SECONDS);
            return;
        }

        int position = Integer.parseInt(b.getId().substring(b.getId().length() - 1));

        // Check if the move is valid
        if (!this.isMoveValid(position))
        {
            Message msg = this._interaction.getChannel()
                    .sendMessage("Your move is invalid! This cell is already occupied. Try with another move.")
                    .complete();

            msg.delete().queueAfter(2, TimeUnit.SECONDS);
            return;
        }

        // Update the board
        List<Button> buttons = new ArrayList<>();

        if (position <= 2)
        {
            buttons = new ArrayList<>(this._row1.getButtons());
            buttons.set(position % 3, Button.secondary(b.getId(), String.valueOf(this._currentPlayer.sign)));

            this._row1 = ActionRow.of(buttons);
            this._board[position] = this._currentPlayer.sign;
        }
        else if (position <= 5)
        {
            buttons = new ArrayList<>(this._row2.getButtons());
            buttons.set(position % 3, Button.secondary(b.getId(), String.valueOf(this._currentPlayer.sign)));

            this._row2 = ActionRow.of(buttons);
            this._board[position] = this._currentPlayer.sign;
        }
        else
        {
            buttons = new ArrayList<>(this._row3.getButtons());
            buttons.set(position % 3, Button.secondary(b.getId(), String.valueOf(this._currentPlayer.sign)));

            this._row3 = ActionRow.of(buttons);
            this._board[position] = this._currentPlayer.sign;
        }

        // Check if there are any matches
        this.checkMatches();

        if (this._winner != ' ')
        {
            if (this._winner == '=')
            {
                this._embed.setDescription("It's a draw");
                this.makeBoardUnclickable();

                this._interaction.getHook().editOriginalEmbeds(this._embed.build()).queue();
                event.editComponents(this._row1, this._row2, this._row3).complete();

                return;
            }

            this._embed.setDescription(this._currentPlayer.user.getName() + " wins! 🎉");
            this.makeBoardUnclickable();

            this._interaction.getHook().editOriginalEmbeds(this._embed.build()).queue();
            event.editComponents(this._row1, this._row2, this._row3).complete();
            return;
        }

        // Set the current player to the next player
        this._currentPlayer = this.nextPlayer(this._currentPlayer);

        this._embed.setDescription(this._currentPlayer.sign + " " + this._currentPlayer.user.getName() + " Your turn!");
        this._interaction.getHook().editOriginalEmbeds(this._embed.build()).queue();
        event.editComponents(this._row1, this._row2, this._row3, this._row4).complete();
    }

    private void checkMatches()
    {
        // Check horizontally.
        for (int row = 0; row < 3; row++)
        {
            char v1 = this._board[this.posToIndex(row, 0)];
            char v2 = this._board[this.posToIndex(row, 1)];
            char v3 = this._board[this.posToIndex(row, 2)];

            if (this.validEquals(v1, v2) && this.validEquals(v2, v3))
                this._winner = v1;
        }

        // Check vertically.
        for (int column = 0; column < 3; column++)
        {
            char v1 = this._board[this.posToIndex(0, column)];
            char v2 = this._board[this.posToIndex(1, column)];
            char v3 = this._board[this.posToIndex(2, column)];

            if (this.validEquals(v1, v2) && this.validEquals(v2, v3))
                this._winner = v1;
        }

        // Check diagonally.
        char middle = this._board[this.posToIndex(1, 1)];
        char topLeft = this._board[this.posToIndex(0, 0)];
        char topRight = this._board[this.posToIndex(0, 2)];
        char bottomLeft = this._board[this.posToIndex(2, 0)];
        char bottomRight = this._board[this.posToIndex(2, 2)];

        if (this.validEquals(topLeft, middle) && this.validEquals(middle, bottomRight))
            this._winner = topLeft;

        if (this.validEquals(topRight, middle) && this.validEquals(middle, bottomLeft))
            this._winner = topRight;

        // Check for draw condition.
        if (this.isBoardFull() && this._winner == ' ')
            this._winner = '=';
    }

    private int posToIndex(int row, int column)
    {
        return row * 3 + column;
    }

    private boolean validEquals(char c1, char c2)
    {
        return (c1 != 0 && c1 == c2);
    }

    private boolean isMoveValid(int position)
    {
        return this._board[position] == 0 ? true : false;
    }

    private boolean isBoardFull()
    {
        boolean isFull = true;

        for (int i = 0; i < this._board.length; i++)
        {
            if (this._board[i] == 0)
            {
                isFull = false;
                break;
            }
        }

        return isFull;
    }

    private Player nextPlayer(Player currentPlayer)
    {
        if (currentPlayer == this._player1)
            return this._player2;
        else
            return this._player1;
    }

    private void makeBoardUnclickable()
    {
        List<Button> buttons1 = new ArrayList<>();
        List<Button> buttons2 = new ArrayList<>();
        List<Button> buttons3 = new ArrayList<>();

        this._row1.getButtons().forEach(button -> {
            buttons1.add(button.asDisabled());
        });

        this._row1 = ActionRow.of(buttons1);

        this._row2.getButtons().forEach(button -> {
            buttons2.add(button.asDisabled());
        });

        this._row2 = ActionRow.of(buttons2);

        this._row3.getButtons().forEach(button -> {
            buttons3.add(button.asDisabled());
        });

        this._row3 = ActionRow.of(buttons3);
    }
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
