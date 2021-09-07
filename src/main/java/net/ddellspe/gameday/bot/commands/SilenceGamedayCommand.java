package net.ddellspe.gameday.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import net.ddellspe.gameday.bot.audio.GamedayAudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SilenceGamedayCommand implements MessageResponseCommand {
  @Override
  public String getName() {
    return "silence";
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
      manager.getScheduler().stop();
      message = "Silencing Gameday Manager";
    } else {
      message = "Gameday manager is not running, no audio playing, currently.";
    }

    return event
        .getMessage()
        .getChannel()
        .flatMap(channel -> channel.createMessage(message))
        .then();
  }
}
