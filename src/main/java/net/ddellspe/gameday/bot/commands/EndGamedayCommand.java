package net.ddellspe.gameday.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import net.ddellspe.gameday.bot.audio.GamedayAudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EndGamedayCommand implements MessageResponseCommand {
  @Override
  public String getName() {
    return "end";
  }

  @Override
  public Snowflake getFilterChannel(Snowflake guildId) {
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    return manager.getChatChannels().get(guildId);
  }

  @Override
  public Mono<Void> handle(MessageCreateEvent event) {
    // This will be guaranteed to be present since we're limiting to Join and Move events
    Snowflake guildId = event.getGuildId().get();
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    Snowflake voiceChannelId = manager.getVoiceChannels().get(guildId);

    final Mono<Boolean> nonBotChannelCountIsZero =
        event
            .getClient()
            .getChannelById(voiceChannelId)
            .cast(VoiceChannel.class)
            .flatMapMany(VoiceChannel::getVoiceStates)
            .flatMap(VoiceState::getMember)
            .filter(member -> !member.isBot())
            .count()
            .map(count -> count == 0);
    return event
        .getMessage()
        .getChannel()
        .flatMap(channel -> channel.createMessage("Stopping Gameday Manager"))
        .doOnNext(___ -> manager.stop())
        .flatMap(___ -> event.getClient().getVoiceConnectionRegistry().getVoiceConnection(guildId))
        .filter(___ -> !manager.isStarted())
        .flatMap(VoiceConnection::disconnect);
  }
}
