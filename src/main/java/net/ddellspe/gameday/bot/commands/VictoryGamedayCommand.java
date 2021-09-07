package net.ddellspe.gameday.bot.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import net.ddellspe.gameday.bot.audio.GamedayAudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VictoryGamedayCommand implements MessageResponseCommand {
  @Override
  public String getName() {
    return "victory";
  }

  @Override
  public Snowflake getFilterChannel(Snowflake guildId) {
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    return manager.getChatChannel();
  }

  @Override
  public Mono<Void> handle(MessageCreateEvent event) {
    // This will be guaranteed to be present since we're limiting to Join and Move events
    Snowflake guildId = event.getGuildId().get();
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);

    final String message;
    if (manager.isStarted()) {
      GamedayAudioManager.PLAYER_MANAGER.loadItemOrdered(
          manager,
          manager.getConfiguration().getVictory(),
          new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
              manager.getScheduler().play(audioTrack, true);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {}

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException e) {}
          });
      message = manager.getConfiguration().getTeam() + " Wins!";
    } else {
      message = null;
    }

    return event
        .getMessage()
        .getChannel()
        .filter(___ -> message != null)
        .flatMap(channel -> channel.createMessage(message))
        .then();
  }
}
