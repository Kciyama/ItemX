package org.btc.itemx.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CropHarvestEvent extends Event implements Cancellable {
    private Entity entity;
    private ItemFrame holder;
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

    public CropHarvestEvent(Entity entity, ItemFrame holder) {
        this.entity = entity;
        this.holder = holder;
    }

    public Entity getEntity() {
        return entity;
    }

    public ItemFrame getHolder() {
        return holder;
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
