package org.redcastlemedia.multitallented.civs.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.MiniMessageImpl;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.text.adapter.bukkit.TextAdapter;
import org.apache.commons.collections.map.HashedMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.placeholderexpansion.PlaceHook;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.*;
import java.util.function.Function;

/**
 * https://docs.adventure.kyori.net/minimessage.html
 */
@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class ChatManager {

    private BukkitAudiences bukkitAudiences;

    private static ChatManager instance;

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
            instance.load();
        }
        return instance;
    }

    public void formatMessage(Player source, Civilian civilian, ChatChannelConfig config,
                              String message, Iterable<Player> recipients) {

        TownManager townManager = TownManager.getInstance();
        String biggestTown = townManager.getBiggestTown(civilian);

        ConfigManager instance = ConfigManager.getInstance();
        Map<String, String> chatTagFormat = instance.getChatTagFormat();

        String nation = PlaceHook.getNation(civilian);

        String format = config.format
                .replace("<town_f>", biggestTown == null ? "" : chatTagFormat.get("town_f").replace("$1", biggestTown))
                .replace("<nation_f>", nation == null ? "" : chatTagFormat.get("nation_f").replace("$1", nation))
                .replace("<message>", message)
                .replaceAll("<player>", source.getName());

        //todo cache MiniMessage later once placeholder resolver supports passing around player context ?
        MiniMessage.Builder mmb = MiniMessage.builder();
        if (Civs.placeholderAPI != null) {
            mmb.placeholderResolver((name) -> {
                if (!name.startsWith("%")) {
                    return null;
                }
                return Component.text(PlaceholderAPI.setPlaceholders(source, name));
            });
        }
        MiniMessage mm = mmb.build();
        Component parse = mm.parse(format);

        for (CommandSender recipient : recipients) {
            Audience sender = bukkitAudiences.sender(recipient);
            sender.sendMessage(parse);
        }
    }

    public void load() {
        this.bukkitAudiences = BukkitAudiences.create(Civs.getInstance());
    }

    public void onDisable() {
        bukkitAudiences.close();
    }
}
