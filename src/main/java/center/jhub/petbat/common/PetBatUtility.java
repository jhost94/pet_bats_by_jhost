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
	
	public static int calculateDamage(int level) {
		return 1 + (level / 5);
	}
	
	public static int calculateDamage(int level, int bonus) {
		return calculateDamage(level) + bonus;
	}
	
	public static long getExperienceToNextLevel(long level) {
		long lvl = level + 1; //11
		long lvlExponent = lvl / 3 * 2; //7
        return (long) Math.floor((lvl / 0.75f) * 20 * lvlExponent) + BASE_XP_TO_LEVEL_UP; // 1.5 * 20 * 3 + 20 = 110  lvl 1->2 = 110 lvl 10 ->12 = 1175
    }
	
	public static long getMissingExperienceToNextLevel(long level, long xp) {
        return getExperienceToNextLevel(level) - xp;
    }
	
	public static int getExperienceToNextLevelInt(long level) {
    	long xp2 = getExperienceToNextLevel(level);
        return (int) Math.min(xp2, Integer.MAX_VALUE);
    }
	
	public static int getExperienceToNextLevelInt(long level, long xp) {
        return (int) (getExperienceToNextLevelInt(level) - xp);
    }
	
	public static void messagePlayer(EntityPlayer player, String message) {
		player.addChatMessage(new ChatComponentText(message));
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
	
	public static String theme1Message(String key, long value) {
		return theme1Message(key, "" + value);
	}

    public static double invertHealthValue(double input, double max) {
        return Math.abs((input * PetBatConstants.INVENTORY_POCKET_BAT_ITEM_MAX_HEALTH)/ max);
    }
}
