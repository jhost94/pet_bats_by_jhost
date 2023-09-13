package center.jhub.petbat.common.item;

import center.jhub.petbat.common.EntityPetBat;
import center.jhub.petbat.common.PetBatMod;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemPocketedPetBat extends Item {

    public ItemPocketedPetBat() {
        super();
        maxStackSize = 1;
        setMaxDamage(28);
        setCreativeTab(CreativeTabs.tabCombat);
    }
    
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        itemIcon = iconRegister.registerIcon("petbat:pocketbat");
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        if (world.isRemote) {
            PetBatMod.proxy.displayGui(itemStack);
        }
        
        return itemStack;
    }
    
    @Override
    public boolean getIsRepairable(ItemStack batStack, ItemStack repairStack) {
        return false;
    }
    
    @Override
    public boolean getShareTag() {
        return true;
    }
    
    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.stackTagCompound != null && PetBatMod.instance().getLevelFromExperience(stack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP")) > 5;
    }
    
    public static ItemStack fromBatEntity(EntityPetBat batEnt) {
        if (batEnt.worldObj.isRemote) {
            return null;
        }
        
        ItemStack batstack = new ItemStack(PetBatMod.instance().itemPocketedBat);
        writeCompoundStringToItemStack(batstack, "display", "Name", batEnt.getDisplayName());
        writeCompoundStringToItemStack(batstack, "petbatmod", "Owner", batEnt.getOwnerName());
        writeCompoundIntegerToItemStack(batstack, "petbatmod", "BatXP", batEnt.getBatExperience());
        writeCompoundFloatToItemStack(batstack, "petbatmod", "health", batEnt.getHealth());
        writeCompoundIntegerToItemStack(batstack, "petbatmod", "BatDamage", batEnt.getBatAttack());
        writeCompoundIntegerToItemStack(batstack, "petbatmod", "BatDamageFrag", batEnt.getBatAttackItemGivenCount());
        batstack.getTagCompound().getCompoundTag("petbatmod").setFloat("health", batEnt.getHealth());
        batstack.setItemDamage((int) invertHealthValue(batEnt.getHealth(), batEnt.getMaxHealth()));
        return batstack;
    }
    
    public static EntityPetBat toBatEntity(World world, ItemStack batStack, EntityPlayer player) {
        EntityPetBat batEnt = new EntityPetBat(world);
        String owner = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getString("Owner") : player.getCommandSenderName();
        String name = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("display").getString("Name") : "Battus Genericus";
        int xp = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP") : 0;
        int atk = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatDamage") : 0;
        int atkFrag = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatDamageFrag") : 0;
        if (owner.equals("")) owner = player.getCommandSenderName();
        if (name.equals("")) name = "Battus Genericus";
        batEnt.setNames(owner, name);
        batEnt.setOwnerEntity(player);
        batEnt.setBatExperience(xp);
        batEnt.setHealth(batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getFloat("health") : batEnt.getMaxHealth());
        batEnt.setAttack(atk);
        batEnt.addBatAttack(atkFrag);
        return batEnt;
    }
    
    public static void writeBatNameToItemStack(ItemStack stack, String name) {
        writeCompoundStringToItemStack(stack, "display", "Name", EnumChatFormatting.DARK_PURPLE + name);
    }
    
    public static String getBatNameFromItemStack(ItemStack stack) {
        return (stack.stackTagCompound != null ? stack.stackTagCompound.getCompoundTag("display").getString("Name") : "Battus Genericus");
    }
    
    /**
     * @param input value to invert
     * @param max maximum health value
     * @return inverted value
     */
    public static double invertHealthValue(double input, double max) {
        return Math.abs(input - max);
    }
    
    public static void writeCompoundIntegerToItemStack(ItemStack stack, String tag, String key, int data) {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setInteger(key, data);
    }
    
    public static void writeCompoundFloatToItemStack(ItemStack stack, String tag, String key, float data) {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setFloat(key, data);
    }

    public static void writeCompoundStringToItemStack(ItemStack stack, String tag, String key, String data) {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setString(key, data);
    }
    
    private static void checkCompoundTag(ItemStack stack, String tag) {
        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }

        if (!stack.stackTagCompound.hasKey(tag)) {
            stack.stackTagCompound.setTag(tag, new NBTTagCompound());
        }
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        return EnumChatFormatting.DARK_PURPLE+super.getItemStackDisplayName(itemStack);
    }
    
}
