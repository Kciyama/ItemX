package org.btc.itemx.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CropPlantEvent extends Event implements Cancellable {
    private Player player;
    private ItemStack seed;
    private String seedId;
    private Block plantOn;
    private static final HandlerList HANDLERS = new HandlerList();
    private Boolean cancelled = false;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public CropPlantEvent(Player player, ItemStack seed, String seedId , Block plantOn) {
        this.player = player;
        this.seed = seed;
        this.seedId = seedId;
        this.plantOn = plantOn;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getSeed() {
        return seed;
    }

    public String getSeedId(){
        return seedId;
    }

    public Block getPlantOn() {
        return plantOn;
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
