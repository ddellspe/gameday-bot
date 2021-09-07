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

  @JsonProperty("team")
  private String team;

  @JsonProperty("touchdown")
  private String touchdown;

  @JsonProperty("field goal")
  private String fieldGoal;

  @JsonProperty("victory")
  private String victory;

  public Snowflake getGuildId() {
    return Snowflake.of(guildId);
  }

  public Snowflake getChatChanelId() {
    return Snowflake.of(chatChannelId);
  }

  public Snowflake getVoiceChannelId() {
    return Snowflake.of(voiceChannelId);
  }

  public String getTeam() {
    return team;
  }

  public String getTouchdown() {
    return touchdown;
  }

  public String getFieldGoal() {
    return fieldGoal;
  }

  public String getVictory() {
    return victory;
  }
}
