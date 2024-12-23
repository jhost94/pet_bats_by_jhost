package center.jhub.petbat.common.item;

import center.jhub.petbat.common.EntityPetBat;
import center.jhub.petbat.common.PetBatConstants;
import center.jhub.petbat.common.PetBatMod;
import center.jhub.petbat.common.PetBatUtility;
import center.jhub.petbat.common.model.HealthIncreaseModel;
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
        setMaxDamage(PetBatConstants.INVENTORY_POCKET_BAT_ITEM_MAX_HEALTH);
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
        return stack.stackTagCompound != null && PetBatMod.instance()
        		.getLevelFromExperience(stack.stackTagCompound
        				.getCompoundTag(PetBatConstants.COMPOUND_TAG)
        				.getInteger(PetBatConstants.COMPOUND_BAT_XP)) > 50;
    }
    
    public static ItemStack fromBatEntity(EntityPetBat batEnt) {
        if (batEnt.worldObj.isRemote) {
            return null;
        }
        
        ItemStack batstack = new ItemStack(PetBatMod.instance().itemPocketedBat);
        writeCompoundStringToItemStack(batstack, PetBatConstants.COMPOUND_TAG_DISPLAY, PetBatConstants.COMPOUND_BAT_NAME, batEnt.getDisplayName());
        writeCompoundStringToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_OWNER, batEnt.getOwnerName());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_XP, batEnt.getBatXP());
        writeCompoundFloatToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_HEALTH, batEnt.getHealth());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_DAMAGE, batEnt.getBatAttack());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_DAMAGE_FRAGMENT, batEnt.getBatAttackItemGivenCount());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_LEVEL, batEnt.getBatLevel());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_MAX_HEALTH, batEnt.getBatMaxHealth());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_MAX_HEALTH_FRAGMENT, batEnt.getBatMaxHealthItemGivenCount());
        writeCompoundIntegerToItemStack(batstack, PetBatConstants.COMPOUND_TAG, PetBatConstants.COMPOUND_BAT_PRESTIGE, batEnt.getBatPrestige());
        batstack.setItemDamage((int) invertHealthValue(batEnt.getHealth(), batEnt.getBatMaxHealthItemGivenCount()));
        return batstack;
    }
    
    public static EntityPetBat toBatEntity(World world, ItemStack batStack, EntityPlayer player) {
        EntityPetBat batEnt = new EntityPetBat(world);
        String owner = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getString(PetBatConstants.COMPOUND_BAT_OWNER) : player.getCommandSenderName();
        String name = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG_DISPLAY).getString(PetBatConstants.COMPOUND_BAT_NAME) : "Battus Genericus";
        int xp = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_XP) : 0;
        int atk = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_DAMAGE) : 0;
        float health = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_HEALTH) : 0f;
        int atkFrag = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_DAMAGE_FRAGMENT) : 0;
        int maxHealth = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_MAX_HEALTH) : 0;
        int maxHealthFrag = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_MAX_HEALTH_FRAGMENT) : 0;
        int level = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_LEVEL) : 1;
        int prestige = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_PRESTIGE) : 0;
        if (owner.equals("")) owner = player.getCommandSenderName();
        if (name.equals("")) name = "Battus Genericus";
        batEnt.addBatMaxHealth(maxHealthFrag);
        batEnt.setNames(owner, name);
        batEnt.setOwnerEntity(player);
        batEnt.setBatExperience(xp);
        batEnt.setAttack(atk);
        batEnt.addBatAttack(atkFrag);
        batEnt.setMaxHealth(level, maxHealth);
        batEnt.setHealth((float) Math.min(health, batEnt.getBatTotalMaxHealth()));
        batEnt.setBatLevel(level);
        batEnt.setBatPrestige(prestige);
        PetBatUtility.messagePlayer(player, "Health: " + health);
        return batEnt;
    }
    
    public static void writeBatNameToItemStack(ItemStack stack, String name) {
        writeCompoundStringToItemStack(stack, PetBatConstants.COMPOUND_TAG_DISPLAY, PetBatConstants.COMPOUND_BAT_NAME, EnumChatFormatting.DARK_PURPLE + name);
    }
    
    public static String getBatNameFromItemStack(ItemStack stack) {
        return (stack.stackTagCompound != null ? stack.stackTagCompound
        		.getCompoundTag(PetBatConstants.COMPOUND_TAG_DISPLAY)
        		.getString(PetBatConstants.COMPOUND_BAT_NAME) 
        		: "Battus Genericus");
    }
    
    /**
     * @param input value to invert
     * @param max maximum health value
     * @return inverted value
     */
    public static double invertHealthValue(double input, double max) {
        return PetBatUtility.invertHealthValue(input, max);
    }
    
    public static void writeCompoundIntegerToItemStack(ItemStack stack, String tag, String key, int data) {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setInteger(key, data);
    }
    
    public static void writeCompoundLongToItemStack(ItemStack stack, String tag, String key, long data) {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setLong(key, data);
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
        return EnumChatFormatting.DARK_PURPLE + super.getItemStackDisplayName(itemStack);
    }
    
}
