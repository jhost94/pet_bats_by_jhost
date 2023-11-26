package center.jhub.petbat.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

import java.util.ArrayList;
import java.util.List;

import center.jhub.petbat.common.batAI.PetBatAIAttack;
import center.jhub.petbat.common.batAI.PetBatAIFindSittingSpot;
import center.jhub.petbat.common.batAI.PetBatAIFlying;
import center.jhub.petbat.common.batAI.PetBatAIOwnerAttacked;
import center.jhub.petbat.common.batAI.PetBatAIOwnerAttacks;
import center.jhub.petbat.common.item.ItemPocketedPetBat;
import center.jhub.petbat.common.model.AttackIncreaseModel;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityPetBat extends EntityCreature implements IEntityAdditionalSpawnData {
	private static final int DATA_WATCHER_IS_HANGING_ID = 16;
	private static final int DATA_WATCHER_XP_ID = 17;
	private static final int DATA_WATCHER_IS_STAYING_ID = 18;
	private static final int DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID = 19;
	private static final int DATA_WATCHER_ATTACK_ID = 20;
	private static final int DATA_WATCHER_LEVEL_ID = 21;

	public static final int BASE_XP_TO_LEVEL_UP = 20;
	public static final int NEDED_ITEMS_FOR_ATTACK_INCREASE = 10;
	private static final Item ITEM_FOR_ATTACK_INCREASE = Items.diamond;
	
    private String ownerName;
    private String petName;
    private EntityPlayer owner;
    private EntityItem foodAttackTarget;
    private boolean fluteOut;
    private boolean isRecalled;

    private int lastOwnerX;
    private int lastOwnerY;
    private int lastOwnerZ;

    private ChunkCoordinates hangSpot;

    
    /**
     * TEMP
     * list of items that have special effects
     * */
    private final List<Item> interactiableItems = new ArrayList();

    public EntityPetBat(World par1World) {
        super(par1World);
        setSize(0.5F, 0.9F);
        setIsBatHanging(false);
        ownerName = "";
        petName = "";
        interactiableItems.add(Items.diamond);
        
        lastOwnerX = lastOwnerY = lastOwnerZ = 0;
        hangSpot = null;
        fluteOut = false;
        isRecalled = false;

        tasks.addTask(1, new PetBatAIAttack(this));
        tasks.addTask(2, new PetBatAIFlying(this));
        tasks.addTask(3, new PetBatAIFindSittingSpot(this));
        targetTasks.addTask(1, new PetBatAIOwnerAttacked(this));
        targetTasks.addTask(2, new PetBatAIOwnerAttacks(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, ownerName);
        ByteBufUtils.writeUTF8String(data, petName);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        ownerName = ByteBufUtils.readUTF8String(data);
        petName = ByteBufUtils.readUTF8String(data);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(DATA_WATCHER_IS_HANGING_ID, (byte) 0);
        // XP
        dataWatcher.addObject(DATA_WATCHER_XP_ID, (int) 0);
        dataWatcher.addObject(DATA_WATCHER_IS_STAYING_ID, (byte) 0);
        // attack items given
        dataWatcher.addObject(DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID, (int) 0);
        // attack
        dataWatcher.addObject(DATA_WATCHER_ATTACK_ID, (int) 0);
        // level
        dataWatcher.addObject(DATA_WATCHER_LEVEL_ID, (int) 0);
    }

    public void setNames(String ownerName, String petName) {
        this.ownerName = ownerName;
        this.petName = petName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Used by PetBat Renderer to display Bat Name
     */
    public String getDisplayName() {
        return petName;
    }

    public EntityPlayer getOwnerEntity() {
        return owner;
    }

    public void setOwnerEntity(EntityPlayer playerEntityByName) {
        owner = playerEntityByName;
    }

    public void updateOwnerCoords() {
        lastOwnerX = (int) (owner.posX + 0.5D);
        lastOwnerY = (int) (owner.posY + 0.5D);
        lastOwnerZ = (int) (owner.posZ + 0.5D);
    }

    public int getLastOwnerX() {
        return lastOwnerX;
    }

    public int getLastOwnerY() {
        return lastOwnerY;
    }

    public int getLastOwnerZ() {
        return lastOwnerZ;
    }

    public void setFoodAttackTarget(EntityItem target) {
        foodAttackTarget = target;
    }

    public EntityItem getFoodAttackTarget() {
        return foodAttackTarget;
    }

    public void setHangingSpot(ChunkCoordinates coords) {
        hangSpot = coords;
    }

    public ChunkCoordinates getHangingSpot() {
        return hangSpot;
    }

    /**
     * can be changed for return [whole condition]
     * */
    public boolean getHasTarget() {
        if (getAttackTarget() != null && getAttackTarget().isEntityAlive()) {
            return true;
        }
        if (getFoodAttackTarget() != null && getFoodAttackTarget().isEntityAlive()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.equals(DamageSource.inWall)) {
            return true;
        }
        if (!worldObj.isRemote) {
            if (getIsBatHanging()) {
                setIsBatHanging(false);
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    public void recallToOwner() {
        isRecalled = true;
    }

    @Override
    public boolean interact(EntityPlayer player) {
    	ItemStack item = player.inventory.getCurrentItem();
    	
    	if (getIsBatHanging() && player.getCommandSenderName().equals(ownerName)) {
    		if	(item != null && interactiableItems.contains(item.getItem())) {
    			this.givePetItem(player, item.getItem());
    		} else {
    			setIsBatStaying(!getIsBatStaying());
    			ChatComponentText msg = new ChatComponentText(petName + ": " + 
    	                (getIsBatStaying() ? StatCollector.translateToLocal("translation.PetBat:staying") + "FUCK"
    	                        : StatCollector.translateToLocal("translation.PetBat:notstaying")));
                player.addChatMessage(msg);
                ChatComponentText debugMsg = new ChatComponentText(EnumChatFormatting.BOLD + "" + EnumChatFormatting.AQUA + "Level: " + EnumChatFormatting.RESET + "" + EnumChatFormatting.BLUE + getBatLevel());
                ChatComponentText debugMsg2 = new ChatComponentText(EnumChatFormatting.BOLD + "" + EnumChatFormatting.AQUA + "XP: " + EnumChatFormatting.RESET + "" + EnumChatFormatting.BLUE + getBatXP());
                ChatComponentText debugMsg11 = new ChatComponentText("Level: " + getBatLevel());
                ChatComponentText debugMsg21 = new ChatComponentText("XP: " + getBatXP());
                
                player.addChatMessage(debugMsg);
                player.addChatMessage(debugMsg2);
                player.addChatMessage(debugMsg11);
                player.addChatMessage(debugMsg21);
    		}
             return true;
    	}
    	
        return false;
    }
    
    private void givePetItem(EntityPlayer player, Item item) {
    	if (item.equals(ITEM_FOR_ATTACK_INCREASE)) {
            player.inventory.consumeInventoryItem(ITEM_FOR_ATTACK_INCREASE);
            AttackIncreaseModel model = addBatAttack(1);
            petAnnounceInChat(player, model);
        }
    }
    
    private void petAnnounceInChat(EntityPlayer player, AttackIncreaseModel model) {
    	if (model.getAttackAttackItemCountAdded() > 0) {
    		player.addChatMessage(new ChatComponentText(petName + " has gained +" + model.getAttackAttackItemCountAdded() + " steps towards gaining ATK."));
    	}
    	if (model.getAttackAdded() > 0) {
    		player.addChatMessage(new ChatComponentText(petName + ": has gained +" + model.getAttackAdded() + " attack."));
    		player.addChatMessage(new ChatComponentText(petName + ": has " + model.getCurrentAttack() + " attack."));
    	}
		player.addChatMessage(new ChatComponentText(petName + " has now " + model.getCurrentAttackItemCount() + "/" + NEDED_ITEMS_FOR_ATTACK_INCREASE + " to the next ATK bonus."));
    }

    /**
     * This is part of where the magic is
     *
     * @param target
     * @return
     */
    @Override
    public boolean attackEntityAsMob(Entity target) {
        long level = getBatLevel();
        int damage = getBatAttack();

        float prevHealth = 0;
        EntityLivingBase livingTarget = null;
        if (target instanceof EntityLivingBase) {
            livingTarget = (EntityLivingBase) target;
            prevHealth = livingTarget.getHealth();
        }

        boolean result = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (result) {
            if (livingTarget != null) {
                float damageDealt = prevHealth - livingTarget.getHealth();
                if (damageDealt > 0) {
                    addBatExperience((int) Math.max(1, damageDealt));
                    if (level > 2) {
                        heal(Math.max(damageDealt / 3, 1));
                    }
                }
            } else {
                addBatExperience(damage);
                if (level > 2) {
                    heal(Math.max(damage / 3, 1));
                }
            }
        }

        return result;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    public void setDeadWithoutRecall() {
        super.setDead();
    }

    @Override
    public void setDead() {
        if (this.owner != null && !worldObj.isRemote) {
            setHealth(1);
            ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
            if (batstack != null) {
                PetBatMod.instance().removeFluteFromPlayer(owner, petName);
                if (owner.getHealth() > 0 && owner.inventory.addItemStackToInventory(batstack)) {
                    worldObj.playSoundAtEntity(owner, "mob.slime.big", 1F, 1F);
                } else {
                    worldObj.playSoundAtEntity(owner, "mob.slime.big", 1F, 1F);
                    worldObj.spawnEntityInWorld(new EntityItem(worldObj, owner.posX, owner.posY, owner.posZ, batstack));
                }
            }
        }

        super.setDead();
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected String getLivingSound() {
        return "mob.bat.idle";
    }

    @Override
    protected String getHurtSound() {
        return "mob.bat.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.bat.death";
    }

    @Override
    public void setInPortal() {
        // Nope
    }

    public boolean getIsBatHanging() {
        return (this.dataWatcher.getWatchableObjectByte(DATA_WATCHER_IS_HANGING_ID) & 1) != 0;
    }

    public void setIsBatHanging(boolean par1) {
        setHangingSpot(null);

        byte isHanging = this.dataWatcher.getWatchableObjectByte(DATA_WATCHER_IS_HANGING_ID);

        if (par1) {
            this.dataWatcher.updateObject(DATA_WATCHER_IS_HANGING_ID, Byte.valueOf((byte) (isHanging | 1)));
        } else {
            this.dataWatcher.updateObject(DATA_WATCHER_IS_HANGING_ID, Byte.valueOf((byte) (isHanging & -2)));
        }
    }

    /**
     * Bat levels up with all damage it inflicts in combat.
     * 
     * @param xp
     *            one experience point for every point of damage inflicted
     */
    private void addBatExperience(int xp) {
        if (!worldObj.isRemote) {
            setBatExperience(Integer.valueOf(getBatExperience() + xp));
        }
    }

    public int getBatExperience() {
        return dataWatcher.getWatchableObjectInt(17);
    }

    public void setBatExperience(int value) {
        dataWatcher.updateObject(DATA_WATCHER_XP_ID, value);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16d + (2 * PetBatMod.instance().getLevelFromExperience(value)));
    }

    public boolean getIsBatStaying() {
        return dataWatcher.getWatchableObjectByte(18) != 0;
    }

    public void setIsBatStaying(boolean cond) {
        dataWatcher.updateObject(18, (byte) (cond ? 1 : 0));
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        super.onUpdate();

        checkOwnerFlute();

        if (this.getIsBatHanging()) {
            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.posY = (double) MathHelper.floor_double(this.posY) + 1.0D - (double) this.height;
        } else {
            this.motionY *= 0.6D;
        }

        if (isRecalled) {
            ItemStack batstack = ItemPocketedPetBat.fromBatEntity(this);
            if (batstack != null) {
                ItemStack flute = PetBatMod.instance().removeFluteFromPlayer(owner, petName);
                if (owner.inventory.addItemStackToInventory(batstack)) {
                    worldObj.playSoundAtEntity(owner, "mob.slime.big", 1F, 1F);
                    setDeadWithoutRecall();
                } else {
                    owner.inventory.addItemStackToInventory(flute);
                }
            }
        }
    }

    private void checkOwnerFlute() {
        if (!fluteOut && owner != null && !worldObj.isRemote) {
            boolean found = false;
            final Item fluteItem = PetBatMod.instance().itemBatFlute;
            for (ItemStack inventoryItem : owner.inventory.mainInventory) {
                if (inventoryItem != null && inventoryItem.getItem() == fluteItem && inventoryItem.stackTagCompound != null) {
                    if (inventoryItem.stackTagCompound.getString("batName").equals(petName)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                ItemStack newflute = new ItemStack(fluteItem, 1, 0);
                newflute.stackTagCompound = new NBTTagCompound();
                newflute.stackTagCompound.setString("batName", petName);
                if (owner.inventory.addItemStackToInventory(newflute)) {
                    fluteOut = true;
                }
            }
        }
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void fall(float par1) {
    }

    @Override
    protected void updateFallState(double par1, boolean par3) {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.dataWatcher.updateObject(16, Byte.valueOf(nbt.getByte("BatFlags")));
        dataWatcher.updateObject(17, Integer.valueOf(nbt.getInteger("BatXP")));
        this.ownerName = nbt.getString("ownerName");
        this.petName = nbt.getString("petName");
        lastOwnerX = nbt.getInteger("lastOwnerX");
        lastOwnerY = nbt.getInteger("lastOwnerY");
        lastOwnerZ = nbt.getInteger("lastOwnerZ");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setByte("BatFlags", this.dataWatcher.getWatchableObjectByte(16));
        nbt.setInteger("BatXP", getBatExperience());
        nbt.setString("ownerName", this.ownerName);
        nbt.setString("petName", this.petName);
        nbt.setInteger("lastOwnerX", lastOwnerX);
        nbt.setInteger("lastOwnerY", lastOwnerY);
        nbt.setInteger("lastOwnerZ", lastOwnerZ);
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere();
    }

    @Override
    public String getCommandSenderName() {
        return petName;
    }

    public void setAttack(int atk) {
    	dataWatcher.updateObject(DATA_WATCHER_ATTACK_ID, atk);
    }
    
    public int getBatAttackItemGivenCount() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID);
    }
    
    public void setBatAttackItemGivenCount(int atkFrag) {
    	dataWatcher.updateObject(DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID, atkFrag);
    }
    
    public int getBatAttack() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_ATTACK_ID);
    }
    
    public long getBatLevel() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_LEVEL_ID);
    }
    
    public long getBatXP() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_XP_ID);
    }
    
    public void setBatLevel(long level) {
    	dataWatcher.updateObject(DATA_WATCHER_LEVEL_ID, level);
    }
    
    public void setBatXP(long xp) {
    	dataWatcher.updateObject(DATA_WATCHER_XP_ID, xp);
    }
    
    public void addBatXP(long xp) {
    	long currentLevel = getBatLevel();
    	long xpNeeded = PetBatMod.instance().getMissingExperienceToNextLevel(currentLevel, xp);
    	if (xp >= xpNeeded) {
    		long lvlsToAdd = (long) Math.floor(xp / xpNeeded);
    		long remainingXp = xp % xpNeeded;
    		setBatXP(remainingXp);
    		setBatLevel(currentLevel + lvlsToAdd);
    	} else {
    		long currentXP = getBatXP();
    		setBatXP(currentXP + xp);
    	}
    }

    public AttackIncreaseModel addBatAttack(int atkItemsGiven) {
    	int initialAttackItemGivenCount = getBatAttackItemGivenCount();
    	int initialAttack = getBatAttack();
    	
    	return new AttackIncreaseModel(
    			initialAttack, 
    			atkItemsGiven, 
    			initialAttackItemGivenCount, 
    			NEDED_ITEMS_FOR_ATTACK_INCREASE, 
    			dataWatcher,
    			DATA_WATCHER_ATTACK_ID,
    			DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID
    			);
    }
}
