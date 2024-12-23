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
import java.util.function.Consumer;

import center.jhub.petbat.common.batAI.PetBatAIAttack;
import center.jhub.petbat.common.batAI.PetBatAIFindSittingSpot;
import center.jhub.petbat.common.batAI.PetBatAIFlying;
import center.jhub.petbat.common.batAI.PetBatAIOwnerAttacked;
import center.jhub.petbat.common.batAI.PetBatAIOwnerAttacks;
import center.jhub.petbat.common.item.ItemPocketedPetBat;
import center.jhub.petbat.common.model.AbstractIncreaseModel;
import center.jhub.petbat.common.model.AttackIncreaseModel;
import center.jhub.petbat.common.model.HealthIncreaseModel;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityPetBat extends EntityCreature implements IEntityAdditionalSpawnData {
	private static final int DATA_WATCHER_IS_HANGING_ID = 16;
	private static final int DATA_WATCHER_XP_ID = 17;
	private static final int DATA_WATCHER_IS_STAYING_ID = 18;
	private static final int DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID = 19;
	private static final int DATA_WATCHER_ATTACK_ID = 20;
	private static final int DATA_WATCHER_LEVEL_ID = 21;
	private static final int DATA_WATCHER_MAX_HEALTH_ID = 22;
	private static final int DATA_WATCHER_MAX_HEALTH_ITEMS_GIVEN_ID = 23;
	private static final int DATA_WATCHER_PRESTIGE_ID = 24;

	public static final int BASE_XP_TO_LEVEL_UP = 20;
	public static final int NEDED_ITEMS_FOR_ATTACK_INCREASE = 10;
	public static final int NEDED_ITEMS_FOR_MAX_HEALTH_INCREASE = 10;
	private static final Item ITEM_FOR_ATTACK_INCREASE = Items.diamond;
	private static final Item ITEM_FOR_HEALTH_INCREASE = Items.iron_chestplate;
	
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
    private final List<Item> interactiableItems = new ArrayList<Item>();

    public EntityPetBat(World par1World) {
        super(par1World);
        setSize(0.5F, 0.9F);
        setIsBatHanging(false);
        ownerName = "";
        petName = "";
        interactiableItems.add(ITEM_FOR_ATTACK_INCREASE);
        interactiableItems.add(ITEM_FOR_HEALTH_INCREASE);
        
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
        // max health
        dataWatcher.addObject(DATA_WATCHER_MAX_HEALTH_ID, (int) PetBatUtility.calculateMaxHealth(1));
        // max health items given
        dataWatcher.addObject(DATA_WATCHER_MAX_HEALTH_ITEMS_GIVEN_ID, (int) 0);
        // prestige
        dataWatcher.addObject(DATA_WATCHER_PRESTIGE_ID, (int) 0);
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
    			Item i = item.getItem();
    			if (i != null && i.equals(PetBatMod.instance().itemBatFlute)) {
    				PetBatUtility.messagePlayer(player, petName + ": " + 
                    		(getIsBatStaying() ? StatCollector.translateToLocal("translation.PetBat:staying")
        	                : StatCollector.translateToLocal("translation.PetBat:notstaying")));
                    List<String> msgs = new ArrayList<String>();
                    msgs.add(PetBatUtility.theme1Message("Level: ", "" + getBatLevel()));
                    msgs.add(PetBatUtility.theme1Message("XP: ", "" + getBatXP()));
                    msgs.add(PetBatUtility.theme1Message("Attack: ", "" + getBatAttack()));
                    msgs.add(PetBatUtility.theme1Message("Bonus max HP: ", "" + getBatMaxHealth()));
                    msgs.add(PetBatUtility.theme1Message("Max HP: ", "" + getMaxHealth()));
                    printMessages(player, msgs);
    			}
    		}
             return true;
    	}
    	
        return false;
    }
    
    private void printMessages(final EntityPlayer player, List<String> messages) {
    	messages.forEach(new Consumer<String>() {
			@Override
			public void accept(String m) {
				PetBatUtility.messagePlayer(player, m);
			}
		});
    }
    
    private void givePetItem(EntityPlayer player, Item item) {
    	if (item.equals(ITEM_FOR_ATTACK_INCREASE)) {
            player.inventory.consumeInventoryItem(ITEM_FOR_ATTACK_INCREASE);
            AbstractIncreaseModel model = addBatAttack(1);
            petAnnounceInChat(player, model);
        }
    	if (item.equals(ITEM_FOR_HEALTH_INCREASE)) {
            player.inventory.consumeInventoryItem(ITEM_FOR_HEALTH_INCREASE);
            AbstractIncreaseModel model = addBatMaxHealth(1);
            setMaxHealth(getBatLevel(), model.getCurrentStat());
            petAnnounceInChat(player, model);
        }
    }
    
    private void petAnnounceInChat(EntityPlayer player, AbstractIncreaseModel model) {
    	if (model.getStatItemCountAdded() > 0) {
    		player.addChatMessage(new ChatComponentText(petName + " has gained +" + model.getStatItemCountAdded() + " steps towards gaining " + model.getStatName() + "."));
    	}
    	if (model.getStatAdded() > 0) {
    		player.addChatMessage(new ChatComponentText(petName + ": has gained +" + model.getStatAdded() + " " + model.getStatName() + "."));
    		player.addChatMessage(new ChatComponentText(petName + ": has " + model.getCurrentStat() + " " + model.getStatName() + "."));
    	}
		player.addChatMessage(new ChatComponentText(petName + " has now " + model.getCurrentStatItemCount() + "/" + NEDED_ITEMS_FOR_ATTACK_INCREASE + " to the next " + model.getStatName() + " bonus."));
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
        return dataWatcher.getWatchableObjectInt(DATA_WATCHER_XP_ID);
    }

    public void setBatExperience(int value) {
        dataWatcher.updateObject(DATA_WATCHER_XP_ID, value);
        getEntityAttribute(SharedMonsterAttributes.maxHealth)
        	.setBaseValue(PetBatUtility.calculateMaxHealth(PetBatMod.instance().getLevelFromExperience(value)));
    }

    public boolean getIsBatStaying() {
        return dataWatcher.getWatchableObjectByte(DATA_WATCHER_IS_STAYING_ID) != 0;
    }

    public void setIsBatStaying(boolean cond) {
        dataWatcher.updateObject(DATA_WATCHER_IS_STAYING_ID, (byte) (cond ? 1 : 0));
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
                if (owner == null || owner.inventory == null) {
                	isRecalled = false; //This should be impossible
                	return;
                }
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
                    if (inventoryItem.stackTagCompound.getString(PetBatConstants.COMPOUND_BAT_NAME).equals(petName)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                ItemStack newflute = new ItemStack(fluteItem, 1, 0);
                newflute.stackTagCompound = new NBTTagCompound();
                newflute.stackTagCompound.setString(PetBatConstants.COMPOUND_BAT_NAME, petName);
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
        this.dataWatcher.updateObject(DATA_WATCHER_IS_HANGING_ID, Byte.valueOf(nbt.getByte(PetBatConstants.COMPOUND_BAT_IS_HANGING)));
        dataWatcher.updateObject(DATA_WATCHER_XP_ID, Integer.valueOf(nbt.getInteger(PetBatConstants.COMPOUND_BAT_XP)));
        this.ownerName = nbt.getString(PetBatConstants.COMPOUND_BAT_OWNER_NAME);
        this.petName = nbt.getString(PetBatConstants.COMPOUND_BAT_PET_NAME);
        lastOwnerX = nbt.getInteger(PetBatConstants.COMPOUND_BAT_LAST_OWNER_X);
        lastOwnerY = nbt.getInteger(PetBatConstants.COMPOUND_BAT_LAST_OWNER_Y);
        lastOwnerZ = nbt.getInteger(PetBatConstants.COMPOUND_BAT_LAST_OWNER_Z);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setByte(PetBatConstants.COMPOUND_BAT_IS_HANGING, this.dataWatcher.getWatchableObjectByte(DATA_WATCHER_IS_HANGING_ID));
        nbt.setInteger(PetBatConstants.COMPOUND_BAT_XP, getBatExperience());
        nbt.setString(PetBatConstants.COMPOUND_BAT_OWNER_NAME, this.ownerName);
        nbt.setString(PetBatConstants.COMPOUND_BAT_PET_NAME, this.petName);
        nbt.setInteger(PetBatConstants.COMPOUND_BAT_LAST_OWNER_X, lastOwnerX);
        nbt.setInteger(PetBatConstants.COMPOUND_BAT_LAST_OWNER_Y, lastOwnerY);
        nbt.setInteger(PetBatConstants.COMPOUND_BAT_LAST_OWNER_Z, lastOwnerZ);
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere();
    }

    @Override
    public String getCommandSenderName() {
        return petName;
    }

    public void addBatXP(long xp) {
    	long currentLevel = getBatLevel();
    	long xpNeeded = PetBatMod.instance().getMissingExperienceToNextLevel(currentLevel, xp);
    	if (xp >= xpNeeded) {
    		long lvlsToAdd = (long) Math.floor(xp / xpNeeded);
    		long remainingXp = xp % xpNeeded;
    		setBatXP(remainingXp > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remainingXp);
    		long totalToAdd = currentLevel + lvlsToAdd;
    		if (totalToAdd > Integer.MAX_VALUE) {
    			incrementBatPrestige();
    			setBatLevel(((int) totalToAdd - Integer.MAX_VALUE));
    		} else {
    			setBatLevel((int) totalToAdd);
    		}
    	} else {
    		long currentXP = getBatXP();
    		setBatXP((int) (currentXP + xp));
    	}
    }

    public AttackIncreaseModel addBatAttack(int atkItemsGiven) {
    	return new AttackIncreaseModel(
    			getBatAttack(), 
    			atkItemsGiven, 
    			getBatAttackItemGivenCount(), 
    			NEDED_ITEMS_FOR_ATTACK_INCREASE, 
    			dataWatcher,
    			DATA_WATCHER_ATTACK_ID,
    			DATA_WATCHER_ATTACK_ITEMS_GIVEN_ID
    			);
    }
    
    public HealthIncreaseModel addBatMaxHealth(int healthItemsGiven) {
    	HealthIncreaseModel model = new HealthIncreaseModel(
    			getBatMaxHealth(), 
    			healthItemsGiven, 
    			getBatMaxHealthItemGivenCount(), 
    			NEDED_ITEMS_FOR_MAX_HEALTH_INCREASE, 
    			dataWatcher,
    			DATA_WATCHER_MAX_HEALTH_ID,
    			DATA_WATCHER_MAX_HEALTH_ITEMS_GIVEN_ID
    			);
    	return model;
    }
    
    public void setAttack(int atk) {
    	dataWatcher.updateObject(DATA_WATCHER_ATTACK_ID, atk);
    }
    
    public void setMaxHealth(long level, int health) {
    	this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(PetBatUtility.calculateMaxHealth(level, health));
    	dataWatcher.updateObject(DATA_WATCHER_MAX_HEALTH_ID, health);
    }
    
    public int getBatMaxHealth() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_MAX_HEALTH_ID);
    }
    
    public double getBatTotalMaxHealth() {
    	return PetBatUtility.calculateMaxHealth(getBatLevel(), getBatMaxHealth());
    }
    
    public int getBatMaxHealthItemGivenCount() {
    	Integer maxHealthItemGivenCount = dataWatcher.getWatchableObjectInt(DATA_WATCHER_MAX_HEALTH_ITEMS_GIVEN_ID);
    	return maxHealthItemGivenCount == null ? 0 : maxHealthItemGivenCount.intValue();
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
    
    public int getBatLevel() {
    	return Math.max(1, dataWatcher.getWatchableObjectInt(DATA_WATCHER_LEVEL_ID));
    }
    
    public int getBatXP() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_XP_ID);
    }
    
    public void setBatLevel(int level) {
    	dataWatcher.updateObject(DATA_WATCHER_LEVEL_ID, level);
    }
    
    public void setBatXP(int xp) {
    	dataWatcher.updateObject(DATA_WATCHER_XP_ID, xp);
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
    
    public void setBatPrestige(int prestige) {
    	dataWatcher.updateObject(DATA_WATCHER_PRESTIGE_ID, prestige);
    }
    
    public int getBatPrestige() {
    	return dataWatcher.getWatchableObjectInt(DATA_WATCHER_PRESTIGE_ID);
    }
    
    public void incrementBatPrestige(int increment) {
    	setBatPrestige(getBatPrestige() + increment);
    }
    
    public void incrementBatPrestige() {
    	incrementBatPrestige(1);
    }
}
