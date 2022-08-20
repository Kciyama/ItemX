package org.btc.itemx.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class CustomBlockPlaceEvent extends Event implements Cancellable {
    private Player player;
    private EquipmentSlot equipmentSlot;
    private Block placedAgainst;
    private Location location;
    private BlockData blockData;
    private static Boolean cancelled = false;


    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public CustomBlockPlaceEvent(Player player, EquipmentSlot equipmentSlot, Block placedAgainst, Location location, BlockData blockData) {
        this.player = player;
        this.equipmentSlot = equipmentSlot;
        this.placedAgainst = placedAgainst;
        this.location = location;
        this.blockData = blockData;
    }

    public Player getPlayer() {
        return player;
    }

    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public Block getPlacedAgainst() {
        return placedAgainst;
    }

    public Location getLocation() {
        return location;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
