package net.ddellspe.gameday.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import net.ddellspe.gameday.bot.audio.GamedayAudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AutoJoin implements VoiceStateTrigger {

  @Override
  public boolean isCorrectEventType(VoiceStateUpdateEvent event) {
    return event.isMoveEvent() || event.isJoinEvent();
  }

  @Override
  public Snowflake getFilterChannel(Snowflake guildId) {
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    return manager.getVoiceChannel();
  }

  @Override
  public Mono<Void> handle(VoiceStateUpdateEvent event) {
    Snowflake guildId = event.getCurrent().getGuildId();
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    Snowflake voiceChannelId = manager.getVoiceChannel();

    final Mono<Boolean> nonBotChannelCountIsGreaterThanZero =
        event
            .getClient()
            .getChannelById(voiceChannelId)
            .cast(VoiceChannel.class)
            .flatMapMany(VoiceChannel::getVoiceStates)
            .flatMap(VoiceState::getMember)
            .filter(member -> !member.isBot())
            .count()
            .map(count -> count > 0);
    return event
        .getClient()
        .getChannelById(voiceChannelId)
        .filterWhen(___ -> nonBotChannelCountIsGreaterThanZero)
        .filter(___ -> manager.isStarted())
        .cast(VoiceChannel.class)
        .flatMap(
            channel ->
                channel.join(spec -> spec.setSelfDeaf(true).setProvider(manager.getProvider())))
        .then();
  }
}
