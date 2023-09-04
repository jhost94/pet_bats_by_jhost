package center.jhub.petbat.client;

import net.minecraft.item.ItemStack;
import center.jhub.petbat.common.EntityPetBat;
import center.jhub.petbat.common.Proxy;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy implements Proxy {
    
    @Override
    public void onModPreInitLoad() {
        RenderingRegistry.registerEntityRenderingHandler(EntityPetBat.class, new RenderPetBat());
    }

    @Override
    public void displayGui(ItemStack itemStack) {
        FMLClientHandler.instance().getClient().displayGuiScreen(new GuiPetBatRename(itemStack));
    }
    
}
