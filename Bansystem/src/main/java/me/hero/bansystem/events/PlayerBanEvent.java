package me.hero.bansystem.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String target;
    private final String staff;
    private final String reason;
    private final String duration;

    public PlayerBanEvent(String target, String staff, String reason, String duration) {
        this.target = target;
        this.staff = staff;
        this.reason = reason;
        this.duration = duration;
    }

    public String getTarget() { return target; }
    public String getStaff() { return staff; }
    public String getReason() { return reason; }
    public String getDuration() { return duration; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}