package me.hero.bansystem.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String targetName;
    private final String staffName;
    private final String reason;
    private final String duration;

    public PlayerBanEvent(String targetName, String staffName, String reason, String duration) {
        super(true); // Async to keep performance high
        this.targetName = targetName;
        this.staffName = staffName;
        this.reason = reason;
        this.duration = duration;
    }

    public String getTargetName() { return targetName; }
    public String getStaffName() { return staffName; }
    public String getReason() { return reason; }
    public String getDuration() { return duration; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}