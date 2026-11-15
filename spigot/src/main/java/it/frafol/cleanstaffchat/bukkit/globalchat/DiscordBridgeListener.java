package it.frafol.cleanstaffchat.bukkit.globalchat;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.objects.Placeholder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Bridges the public Minecraft chat with a Discord text channel.
 * Minecraft -> Discord: every (non-cancelled) chat message is sent to the configured channel.
 * Discord -> Minecraft: every message in the configured channel is broadcast in game.
 */
public class DiscordBridgeListener extends ListenerAdapter implements Listener {

    private final CleanStaffChat plugin;

    public DiscordBridgeListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    // ---------------- Minecraft -> Discord ----------------

    // MONITOR + ignoreCancelled: staffchat/adminchat/donorchat and muted messages are cancelled
    // earlier, so they never leak into the public Discord channel.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!enabled(SpigotDiscordConfig.GLOBALCHAT_MC_TO_DISCORD)) return;
        Player player = event.getPlayer();
        send(SpigotDiscordConfig.GLOBALCHAT_TO_DISCORD_FORMAT.get(String.class)
                .replace("%user%", escape(player.getName()))
                .replace("%displayname%", escape(Placeholder.stripToDiscord(player.getDisplayName())))
                .replace("%message%", escape(Placeholder.stripToDiscord(event.getMessage()))));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!enabled(SpigotDiscordConfig.GLOBALCHAT_JOIN_QUIT)) return;
        send(SpigotDiscordConfig.GLOBALCHAT_JOIN_FORMAT.get(String.class)
                .replace("%user%", escape(event.getPlayer().getName())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (!enabled(SpigotDiscordConfig.GLOBALCHAT_JOIN_QUIT)) return;
        send(SpigotDiscordConfig.GLOBALCHAT_QUIT_FORMAT.get(String.class)
                .replace("%user%", escape(event.getPlayer().getName())));
    }

    private void send(String content) {
        JDA jda = plugin.getJda();
        if (jda == null || content == null || content.isEmpty()) return;
        String channelId = SpigotDiscordConfig.GLOBALCHAT_CHANNEL_ID.get(String.class);
        if (channelId == null || channelId.isEmpty()) return;
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) return;
        if (content.length() > 2000) content = content.substring(0, 2000);
        // No @everyone / @here / role / user pings from Minecraft players.
        channel.sendMessage(content).setAllowedMentions(Collections.emptyList()).queue();
    }

    private static String escape(String text) {
        return text == null ? "" : MarkdownSanitizer.escape(text);
    }

    // ---------------- Discord -> Minecraft ----------------

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!enabled(SpigotDiscordConfig.GLOBALCHAT_DISCORD_TO_MC)) return;
        if (!event.getChannel().getId().equals(SpigotDiscordConfig.GLOBALCHAT_CHANNEL_ID.get(String.class))) return;
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return; // avoid loops
        if (event.getAuthor().isBot() && !SpigotDiscordConfig.FORWARD_BOT.get(Boolean.class)) return;
        if (event.isWebhookMessage()) return;

        String message = event.getMessage().getContentDisplay();
        if (!event.getMessage().getAttachments().isEmpty()) {
            message = (message + " [attachment]").trim();
        }
        if (message.isEmpty()) return;

        String username = event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName();
        if (!enabled(SpigotDiscordConfig.GLOBALCHAT_ALLOW_COLORS)) {
            message = ChatColor.stripColor(message.replace("§", "")).replace("&", "&​");
        }
        username = ChatColor.stripColor(username.replace("§", "")).replace("&", "&​");

        String formatted = ChatColor.translateAlternateColorCodes('&', SpigotDiscordConfig.GLOBALCHAT_TO_MC_FORMAT.get(String.class)
                .replace("%user%", event.getAuthor().getName().replace("&", "&​"))
                .replace("%username%", username)
                .replace("%message%", message))
                .replace("​", "");

        UniversalScheduler.getScheduler(plugin).runTask(() -> {
            plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(formatted));
            plugin.getServer().getConsoleSender().sendMessage(formatted);
        });
    }

    private static boolean enabled(SpigotDiscordConfig option) {
        return Boolean.TRUE.equals(option.get(Boolean.class));
    }
}
