package net.ddellspe.gameday.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.util.Snowflake;

public class GuildConfiguration {
  @JsonProperty("guildId")
  private String guildId;

  @JsonProperty("chatChannelId")
  private String chatChannelId;

  @JsonProperty("voiceChannelId")
  private String voiceChannelId;

  public Snowflake getGuildId() {
    return Snowflake.of(guildId);
  }

  public Snowflake getChatChanelId() {
    return Snowflake.of(chatChannelId);
  }

  public Snowflake getVoiceChannelId() {
    return Snowflake.of(voiceChannelId);
  }
}
