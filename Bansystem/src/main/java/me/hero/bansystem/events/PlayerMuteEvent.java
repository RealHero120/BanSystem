// java
package me.hero.bansystem.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMuteEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String targetName;
    private final String staff;
    private final String reason;
    private final String durationReadable;

    public PlayerMuteEvent(String targetName, String staff, String reason, String durationReadable) {
        this.targetName = targetName;
        this.staff = staff;
        this.reason = reason;
        this.durationReadable = durationReadable;
    }

    public String getTargetName() { return targetName; }
    public String getStaff() { return staff; }
    public String getReason() { return reason; }
    public String getDurationReadable() { return durationReadable; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
