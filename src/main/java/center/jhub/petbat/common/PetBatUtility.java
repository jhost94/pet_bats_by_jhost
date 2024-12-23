package center.jhub.petbat.common;

import static center.jhub.petbat.common.EntityPetBat.BASE_XP_TO_LEVEL_UP;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public final class PetBatUtility {
	
	public static final String[][] THEMES = new String[][] {
		{"" + EnumChatFormatting.RESET + EnumChatFormatting.BOLD + EnumChatFormatting.AQUA, "" + EnumChatFormatting.RESET + EnumChatFormatting.BLUE}
	};
	
	private PetBatUtility() {}

	public static double calculateMaxHealth(long level) {
		return 16d + level;
	}
	
	public static double calculateMaxHealth(long level, int bonus) {
		return calculateMaxHealth(level) + bonus;
	}
	
	public static long getMissingExperienceToNextLevel(long level, long xp) {
        /** old system
        if (xp < 25) return 25-xp;
        if (xp < 75) return 75-xp;
        if (xp < 175) return 175-xp;
        if (xp < 375) return 375-xp;
        if (xp < 775) return 775-xp;
        if (xp < 1575) return 1575-xp;
        return -1;
         */
        //if (xp < 25) return 25-xp;
        //if (xp < 75) return 75-xp;
        //return getRequiredExpForUpperLevel(getUpperLevel(xp) + 1);
        return (long) Math.floor((level / 0.75f) * 100) + BASE_XP_TO_LEVEL_UP;
    }
	
	public static void messagePlayer(EntityPlayer player, String message) {
		player.addChatMessage(new ChatComponentText(message));
	}
	
	public static long getLevelFromExperience(long xp) {
        /** Old system
        if (xp < 25) return 0;
        if (xp < 75) return 1;
        if (xp < 175) return 2;
        if (xp < 375) return 3;
        if (xp < 775) return 4;
        if (xp < 1575) return 5;
        return 6;
         */
        if (xp < 25) return 0;
        if (xp < 100) return 1;
        return getUpperLevel(xp);
    }
	
	public static String themeMessage(int theme, String key, String value) {
		return THEMES[theme - 1][0] + key + THEMES[theme - 1][1] + value;
	}
	
	public static String theme1Message(String key, String value) {
		return themeMessage(1, key, value);
	}
	
	public static String theme1Message(String key, int value) {
		return theme1Message(key, "" + value);
	}

    /**
     * change later
     */
    private static long getUpperLevel(long xp) {
        return (int) Math.max(Math.floor(Math.ceil(xp / 100) * 0.75f), 2);
    }
    
    public static double invertHealthValue(double input, double max) {
        return Math.abs((input * PetBatConstants.INVENTORY_POCKET_BAT_ITEM_MAX_HEALTH)/ max);
    }
}
