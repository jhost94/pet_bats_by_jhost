package center.jhub.petbat.common;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import center.jhub.petbat.common.item.ItemBatFlute;
import center.jhub.petbat.common.item.ItemPocketedPetBat;
import center.jhub.petbat.common.network.BatNamePacket;
import center.jhub.petbat.common.network.NetworkHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "PetBat", name = "Pet Bat", version = "2.0.0")
public class PetBatMod implements Proxy {
    private Item TAME_ITEM_ID;
    public final byte BAT_MAX_LVL = 1;

    private Field entityBatFlightCoords;
    public Item itemPocketedBat;
    public Configuration config;
    public Item itemBatFlute;
    private boolean batInventoryTeleport;
    
    @SidedProxy(clientSide = "center.jhub.petbat.client.ClientProxy", serverSide = "center.jhub.petbat.common.PetBatMod")
    public static Proxy proxy;
    
    @Instance(value = "PetBat")
    private static PetBatMod instance;

    public NetworkHelper networkHelper;
    
    public static PetBatMod instance() {
        return instance;
    }
    
    /**
     * Interesting
     */
    private final String[] batNames = {
            "Lucius",
            "Draco",
            "Vlad",
            "Darkwing",
            "Zubat",
            "Cecil",
            "Dragos",
            "Cezar",
            "Ciprian",
            "Daniel",
            "Dorin",
            "Mihai",
            "Mircea",
            "Radu"
    };
    
    public String getLevelTitle(long level) {
        long finalLevel = Math.min(6, level);
        return StatCollector.translateToLocal("translation.PetBat:batlevel" + finalLevel);
    }
    
    public String getLevelDescription(long level) {
        long finalLevel = Math.min(6, level);
        return StatCollector.translateToLocal("translation.PetBat:batlevel" + finalLevel + "desc");
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        try {
            config.load();
			batInventoryTeleport = config.get(Configuration.CATEGORY_GENERAL, "teleportIntoInventory", true).getBoolean(true);
        } catch (Exception e) {
            System.err.println("PetBat has a problem loading it's configuration!");
        } finally {
            config.save();
        }
        
        itemPocketedBat = new ItemPocketedPetBat().setUnlocalizedName("fed_pet_bat");
        GameRegistry.registerItem(itemPocketedBat, "fed_pet_bat");
        
        itemBatFlute = new ItemBatFlute().setUnlocalizedName("bat_flute");
        GameRegistry.registerItem(itemBatFlute, "bat_flute");
        
        networkHelper = new NetworkHelper("AS_PB", BatNamePacket.class);
        
        EntityRegistry.registerModEntity(EntityPetBat.class, "PetBat", 1, this, 25, 5, true);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        proxy.onModPreInitLoad();
        
        entityBatFlightCoords = EntityBat.class.getDeclaredFields()[0];
        entityBatFlightCoords.setAccessible(true);
    }
    
    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt) {
        TAME_ITEM_ID = Items.pumpkin_pie;
    }
	
	public boolean getPetBatInventoryTeleportEnabled() {
	    return batInventoryTeleport;
	}
    
    @SubscribeEvent
    public void onPlayerLeftClick(BreakSpeed event) {
        EntityPlayer p = event.entityPlayer;
        ItemStack item = p.inventory.getCurrentItem();
        if (item != null && item.getItem().equals(TAME_ITEM_ID)) {
            @SuppressWarnings("unchecked")
            List<Entity> entityList = p.worldObj.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox.expand(10D, 10D, 10D));
            ChunkCoordinates coords = new ChunkCoordinates((int)(p.posX+0.5D), (int)(p.posY+1.5D), (int)(p.posZ+0.5D));
            for (Entity ent : entityList) {
                if (ent instanceof EntityBat) {
                    try {
                        entityBatFlightCoords.set(ent, coords);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.target instanceof EntityBat) {
           this.tamePet(event);
        }
    }
    
    private void tamePet(EntityInteractEvent event) {
    	 EntityPlayer p = event.entityPlayer;
         if (!p.worldObj.isRemote) {
             ItemStack item = p.inventory.getCurrentItem();
             if (item != null && item.getItem() == TAME_ITEM_ID) {
                 event.setCanceled(true);
                 p.inventory.consumeInventoryItem(TAME_ITEM_ID);
                 
                 EntityBat b = (EntityBat) event.target;
                 EntityPetBat newPet = new EntityPetBat(p.worldObj);
                 newPet.setLocationAndAngles(b.posX, b.posY, b.posZ, b.rotationYaw, b.rotationPitch);
                 newPet.setNames(p.getGameProfile().getName(), getRandomBatName());
                 newPet.setOwnerEntity(p);
                 
                 p.worldObj.spawnEntityInWorld(newPet);
                 b.setDead();
             }
         }
    }
    
    @SubscribeEvent
    public void onPlayerAttacksEntity(AttackEntityEvent event) {
        if (event.target instanceof EntityPetBat) {
            EntityPetBat bat = (EntityPetBat) event.target;
            if (bat.getOwnerName().equals(event.entityPlayer.getCommandSenderName()) && event.entityPlayer.getCurrentEquippedItem() == null) {
                bat.recallToOwner();
                event.setCanceled(true);
            }
        }
    }
    
    private String getRandomBatName(){
        return batNames[new Random().nextInt(batNames.length)];
    }
    
//    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (!event.entity.worldObj.isRemote) {
            EntityItem itemDropped = event.entityItem;
            EntityItem foundItem;
            final Item id = itemDropped.getEntityItem().getItem();
            
            if (id.equals(itemBatFlute)) { // bat flutes cannot be dropped. ever.
                event.setCanceled(true);
                return;
            }
            
            if (id.equals(itemPocketedBat)) {
                final EntityPetBat bat = ItemPocketedPetBat.toBatEntity(itemDropped.worldObj, itemDropped.getEntityItem(), event.player);
                if (bat.getHealth() > 1) {
                    bat.setPosition(itemDropped.posX, itemDropped.posY, itemDropped.posZ);
                    itemDropped.worldObj.spawnEntityInWorld(bat);
                    event.setCanceled(true);
                } else {
                    // bat is inert. see if it was tossed onto pumpkin pie for revival
                    
                    final List nearEnts = itemDropped.worldObj.getEntitiesWithinAABBExcludingEntity(itemDropped, itemDropped.boundingBox.expand(8D, 8D, 8D));
                    for (Object o : nearEnts) {
                        if (o instanceof EntityItem) {
                            foundItem = (EntityItem) o;
                            if (foundItem.getEntityItem().getItem() == TAME_ITEM_ID) {
                                bat.setPosition(itemDropped.posX, itemDropped.posY, itemDropped.posZ);
                                itemDropped.worldObj.spawnEntityInWorld(bat);
                                bat.setHealth(bat.getMaxHealth()); // set full entity health
                                event.setCanceled(true);
                                foundItem.getEntityItem().stackSize--;
                                if (foundItem.getEntityItem().stackSize < 1) {
                                    foundItem.setDead(); // destroy pie item
                                }
                                break;
                            }
                        }
                    }
                }
            } else if (id.equals(TAME_ITEM_ID)) {
                final List nearEnts = itemDropped.worldObj.getEntitiesWithinAABBExcludingEntity(itemDropped, itemDropped.boundingBox.expand(8D, 8D, 8D));
                for (Object o : nearEnts) {
                    if (o instanceof EntityPetBat) {
                        final EntityPetBat bat = (EntityPetBat) o;
                        if ((bat.getAttackTarget() == null || !bat.getAttackTarget().isEntityAlive())
                                && (bat.getFoodAttackTarget() == null || bat.getFoodAttackTarget().isEntityAlive())) {
                            bat.setFoodAttackTarget(itemDropped);
                            break;
                        }
                    } else if (o instanceof EntityItem) {
                        foundItem = (EntityItem) o;
                        if (foundItem.getEntityItem().getItem() == itemPocketedBat) { // inert bat lying around
                            final EntityPetBat bat = ItemPocketedPetBat.toBatEntity(foundItem.worldObj, foundItem.getEntityItem(), event.player);
                            bat.setPosition(foundItem.posX, foundItem.posY, foundItem.posZ);
                            foundItem.worldObj.spawnEntityInWorld(bat);
                            bat.setHealth(bat.getMaxHealth()); // set full entity health
                            event.setCanceled(true);
                            foundItem.setDead(); // destroy bat item
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerDropsEvent(PlayerDropsEvent event) {
        // iterate drops, remove all batflutes
        final Iterator<EntityItem> iter = event.drops.iterator();
        while (iter.hasNext()) {
            if (iter.next().getEntityItem().getItem() == itemBatFlute) {
                iter.remove();
            }
        }
    }
    
    @SubscribeEvent
    public void onEntityLivingUpdate(LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer) event.entityLiving;
            if (p.isEntityAlive() && p.getCurrentEquippedItem() != null && p.getCurrentEquippedItem().getItem().equals(itemPocketedBat)) {
                if (p.getActivePotionEffect(Potion.nightVision) == null) {
                    p.addPotionEffect(new PotionEffect(Potion.nightVision.id, 100));
                }
            }
        }
    }

    @Override
    public void onModPreInitLoad() {
        // NOOP, Proxy only relevant on client
    }

    @Override
    public void displayGui(ItemStack itemStack) {
        // NOOP, Proxy only relevant on client
    }
    
    public ItemStack removeFluteFromPlayer(EntityPlayer player, String petName) {
    	if (player == null || player.inventory == null || player.inventory.mainInventory == null) return null;
    	
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack item = player.inventory.mainInventory[i];
            if (item != null && item.getItem() == itemBatFlute) {
                if (item.stackTagCompound != null && item.stackTagCompound.getString(PetBatConstants.COMPOUND_BAT_NAME).equals(petName)) {
                    player.inventory.setInventorySlotContents(i, null);
                    return item;
                }
            }
        }
        return null;
    }
}
