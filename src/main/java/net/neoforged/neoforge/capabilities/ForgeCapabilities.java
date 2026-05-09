package net.neoforged.neoforge.capabilities;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Minimal compatibility constants for legacy capability lookups.
 */
public final class ForgeCapabilities {
  private ForgeCapabilities() {}

  public static final Capability<IFluidHandler> FLUID_HANDLER = new Capability<>("fluid_handler");
  public static final Capability<IFluidHandlerItem> FLUID_HANDLER_ITEM = new Capability<>("fluid_handler_item");
  public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");
}
