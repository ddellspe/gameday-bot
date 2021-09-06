package net.ddellspe.gameday.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface MessageResponseCommand {
  String getName();

  Snowflake getFilterChannel(Snowflake guildId);

  Mono<Void> handle(MessageCreateEvent event);
}
