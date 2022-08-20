package org.btc.itemx.events;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CropGrowEvent extends Event implements Cancellable {
    private ItemFrame holder;
    private ItemStack oldStage;
    private ItemStack newStage;
    private static final HandlerList HANDLERS = new HandlerList();
    private Boolean cancelled = false;

    public CropGrowEvent(ItemFrame holder, ItemStack oldStage, ItemStack newStage) {
        this.holder = holder;
        this.oldStage = oldStage;
        this.newStage = newStage;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public ItemFrame getHolder() {
        return holder;
    }

    public ItemStack getOldStage() {
        return oldStage;
    }

    public ItemStack getNewStage() {
        return newStage;
    }
}
