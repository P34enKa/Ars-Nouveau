package com.hollingsworth.craftedmagic.client.keybindings;


import com.hollingsworth.craftedmagic.ArsNouveau;
import com.hollingsworth.craftedmagic.api.CraftedMagicAPI;
import com.hollingsworth.craftedmagic.client.gui.GuiRadialMenu;
import com.hollingsworth.craftedmagic.client.gui.GuiSpellBook;
import com.hollingsworth.craftedmagic.items.SpellBook;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ArsNouveau.MODID)
public class KeyHandler {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();


    @SubscribeEvent
    public static void clientTick(final TickEvent.ClientTickEvent event) {
        while(ModKeyBindings.OPEN_CRAFTING_HUD.isKeyDown()){
            if(MINECRAFT.currentScreen != null)
                return;
            ItemStack stack = MINECRAFT.player.getHeldItemMainhand();
            if(stack.getItem() instanceof SpellBook && stack.hasTag()){
                MINECRAFT.displayGuiScreen(new GuiRadialMenu(ModKeyBindings.OPEN_CRAFTING_HUD, stack.getTag()));
            }
        }

        if (event.phase != TickEvent.Phase.END) return;

        if (ModKeyBindings.OPEN_BOOK.isKeyDown()) {
            ItemStack stack = MINECRAFT.player.getHeldItemMainhand();
            if(stack.getItem() instanceof SpellBook && stack.hasTag()){
                CompoundNBT tag = stack.getTag();
                GuiSpellBook.open(CraftedMagicAPI.getInstance(), tag, ((SpellBook) stack.getItem()).getTier().ordinal());
            }
        }

    }
}