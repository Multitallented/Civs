package org.redcastlemedia.multitallented.civs.towns;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.redcastlemedia.multitallented.civs.Civs;


public class TownBar
{
    private final Deque<Notification> notificationsDeque = new ArrayDeque<>();
    public BossBar bossBar;
    public String townName;
    public BarColor defaultBarColor = BarColor.WHITE;
    public BarStyle defaultBarStyle = BarStyle.SEGMENTED_10;
    public ChatColor defaultTitleColor = ChatColor.WHITE;

    public TownBar(String townName) {
        this.townName = townName;
        if (Civs.getInstance() == null) {
            return;
        }
        this.bossBar = Bukkit.createBossBar(townName, defaultBarColor, defaultBarStyle);
        update();
    }

    private double clamp(double x, double min, double max) {
        return Math.max(min, Math.min(max, x));
    }

    public void update() {
        Town town = TownManager.getInstance().getTown(townName);
        bossBar.setProgress(clamp((double) town.getPower() / town.getMaxPower(), 0, 1));
    }

    public void process() {
        Notification notification = notificationsDeque.peekFirst();

        if (notification == null)
        {
            reset();
            return;
        }

        bossBar.setTitle(notification.title);
        bossBar.setColor(notification.color);

        notification.runTaskLater(Civs.getInstance(), 20 * notification.duration);
    }

    public void reset()
    {
        bossBar.setTitle(defaultTitleColor + townName);
        bossBar.setColor(defaultBarColor);
    }

    public void addPlayer(UUID uuid)
    {
        bossBar.addPlayer(Bukkit.getPlayer(uuid));
    }

    public void removePlayer(UUID uuid)
    {
        bossBar.removePlayer(Bukkit.getPlayer(uuid));
    }

    public void removeAllPlayers()
    {
        bossBar.removeAll();
    }

    public void addNotification(String title, BarColor color, long duration)
    {
        boolean is_empty = notificationsDeque.isEmpty();

        notificationsDeque.add(new Notification(defaultTitleColor + townName + " - " + title, color, duration));

        if (is_empty)
        {
            process();
        }
    }

    public void removeNotification()
    {
        notificationsDeque.poll();
    }

    private class Notification extends BukkitRunnable
    {
        public String title;
        public BarColor color;
        public long duration;

        public Notification(String title, BarColor color, long duration)
        {
            this.title = title;
            this.color = color;
            this.duration = duration;
        }

        @Override
        public void run()
        {
            TownBar.this.removeNotification();
            TownBar.this.process();
        }
    }

}
