package it.frafol.cleanstaffchat.bukkit.enums;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;

public enum SpigotDiscordConfig {

    USE_EMBED("discord.embeds.use_embeds"),

    EMBEDS_STAFFCHATCOLOR("discord.embeds.staffchat_color"),
    EMBEDS_ADMINCHATCOLOR("discord.embeds.adminchat_color"),
    EMBEDS_DONORCHATCOLOR("discord.embeds.donorchat_color"),

    EMBEDS_FOOTER("discord.embeds.footer"),
    FORWARD_BOT("discord.forward_bot_messages"),

    STAFFCHAT_EMBED_TITLE("discord.staffchat_embed_title"),
    ADMINCHAT_EMBED_TITLE("discord.adminchat_embed_title"),
    DONORCHAT_EMBED_TITLE("discord.donorchat_embed_title"),
    STAFFLIST_EMBED_TITLE("discord.stafflist_embed_title"),

    DISCORD_ENABLED("discord.enabled"),
    DISCORD_TOKEN("discord.token"),
    DISCORD_ACTIVITY_TYPE("discord.activity_type"),
    DISCORD_ACTIVITY("discord.activity"),
    STAFF_CHANNEL_ID("discord.staffchat_channel_id"),
    DONOR_CHANNEL_ID("discord.donorchat_channel_id"),
    STAFFLIST_CHANNEL_ID("discord.stafflist_channel_id"),
    ADMIN_CHANNEL_ID("discord.adminchat_channel_id"),

    GLOBALCHAT_ENABLED("discord.globalchat.enabled"),
    GLOBALCHAT_CHANNEL_ID("discord.globalchat.channel_id"),
    GLOBALCHAT_MC_TO_DISCORD("discord.globalchat.minecraft_to_discord"),
    GLOBALCHAT_DISCORD_TO_MC("discord.globalchat.discord_to_minecraft"),
    GLOBALCHAT_JOIN_QUIT("discord.globalchat.join_quit_messages"),
    GLOBALCHAT_TO_DISCORD_FORMAT("discord.globalchat.to_discord_format"),
    GLOBALCHAT_JOIN_FORMAT("discord.globalchat.join_format"),
    GLOBALCHAT_QUIT_FORMAT("discord.globalchat.quit_format"),
    GLOBALCHAT_TO_MC_FORMAT("discord.globalchat.to_minecraft_format"),
    GLOBALCHAT_ALLOW_COLORS("discord.globalchat.allow_color_codes");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    SpigotDiscordConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getDiscordTextFile().get(path));
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }
}
