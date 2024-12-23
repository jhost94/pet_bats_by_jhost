package center.jhub.petbat.common.item;

import center.jhub.petbat.common.EntityPetBat;
import center.jhub.petbat.common.PetBatConstants;
import center.jhub.petbat.common.PetBatUtility;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemBatFlute extends Item {

    public ItemBatFlute() {
        super();
        maxStackSize = 1;
        setMaxDamage(0);
    }
    
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        itemIcon = iconRegister.registerIcon("petbat:batflute");
    }
    
    @Override
    public boolean getShareTag() {
        return true;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        EntityPetBat bat;
        if (itemStack.stackTagCompound != null) {
            String batname = itemStack.stackTagCompound.getString(PetBatConstants.COMPOUND_BAT_NAME);
            for (int i = 0; i < world.loadedEntityList.size(); i++) {
                if (world.loadedEntityList.get(i) instanceof EntityPetBat) {
                    bat = (EntityPetBat) world.loadedEntityList.get(i);
                    if (bat.getDisplayName().equals(batname)) {
                        bat.recallToOwner();
                        itemStack.stackSize = 0;
                    }
                }
            }
            return itemStack;
        }
        return null;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        String batname = itemStack.stackTagCompound != null ? (": " + itemStack.stackTagCompound.getString(PetBatConstants.COMPOUND_BAT_NAME)) : ": unassigned";
        return EnumChatFormatting.GOLD + super.getItemStackDisplayName(itemStack) + batname;
    }
    
}
