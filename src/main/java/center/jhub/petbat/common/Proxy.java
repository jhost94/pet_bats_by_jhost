package center.jhub.petbat.common;

import net.minecraft.item.ItemStack;

public interface Proxy {
    public void onModPreInitLoad();
    public void displayGui(ItemStack itemStack);
}
