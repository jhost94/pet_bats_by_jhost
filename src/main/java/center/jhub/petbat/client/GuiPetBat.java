package center.jhub.petbat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import center.jhub.petbat.common.EntityPetBat;
import center.jhub.petbat.common.GradientOrientation;
import center.jhub.petbat.common.PetBatConstants;
import center.jhub.petbat.common.PetBatMod;
import center.jhub.petbat.common.PetBatUtility;
import center.jhub.petbat.common.item.ItemPocketedPetBat;
import center.jhub.petbat.common.network.BatNamePacket;

public class GuiPetBat extends GuiScreen {
	public static final int PROGRESS_BAR_BG_HEIGHT = 15;
	public static final int PROGRESS_BAR_INNER_BORDER = 2;
	public static final int PROGRESS_BAR_OUTER_BORDER = 2;
	
    private final String screenTitle;
    private final ItemStack petBatItemStack;
    private GuiTextField textfield;

    private final int xp;
    private final int xpToNext;
    private final int level;
    private final double maxHealth;
    private final double health;
    private final int attackStrength;
    private final int attackIncCount;
    private final int attackIncCountMax;
    private final String levelTitle;
    private final String levelDesc;

    public GuiPetBat(ItemStack stack) {
        petBatItemStack = stack;
        screenTitle = StatCollector.translateToLocal("translation.PetBat:gui_title");

        xp = stack.stackTagCompound != null ? 
        		stack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_XP) 
        		: 0;
        level = stack.stackTagCompound != null ? 
        		stack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_LEVEL) 
        		: 0;
        xpToNext = PetBatUtility.getExperienceToNextLevelInt(level, xp);
        maxHealth = PetBatUtility.calculateMaxHealth(level, stack.stackTagCompound != null ? 
        		stack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_MAX_HEALTH)
        		: 0);
        health = stack.stackTagCompound != null ? 
        		stack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_HEALTH) 
        		: 0;
        attackStrength = stack.stackTagCompound != null ? 
        		stack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_DAMAGE) 
        		: 1;
        attackIncCount = stack.stackTagCompound != null ? 
        		stack.stackTagCompound.getCompoundTag(PetBatConstants.COMPOUND_TAG).getInteger(PetBatConstants.COMPOUND_BAT_DAMAGE_FRAGMENT) 
        		: 0;
        attackIncCountMax = EntityPetBat.NEDED_ITEMS_FOR_ATTACK_INCREASE;
        levelTitle = PetBatMod.instance().getLevelTitle(level);
        levelDesc = PetBatMod.instance().getLevelDescription(level);
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        textfield = new GuiTextField(fontRendererObj, this.width / 2 - 75, 60, 150, 20);
        textfield.setTextColor(-1);
        textfield.setMaxStringLength(30);
        textfield.setFocused(true);
        textfield.setText(ItemPocketedPetBat.getBatNameFromItemStack(petBatItemStack));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (textfield.textboxKeyTyped(par1, par2)) {
            if (!textfield.getText().equals("")) {
                PetBatMod.instance().networkHelper.sendPacketToServer(new BatNamePacket(Minecraft.getMinecraft().thePlayer.getCommandSenderName(),
                        textfield.getText()));
            }
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        this.textfield.mouseClicked(par1, par2, par3);
    }

    @Override
    public void updateScreen() {
        textfield.updateCursorCounter();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
    	////
    	int genericTextCoulor = 0xFFFFFF;
    	int titleTextCoulor = 0x0000AA;
    	////
        this.drawDefaultBackground();

        int x = this.width / 2;
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, x, 40, titleTextCoulor);

        int y = 100;
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:level")
                + EnumChatFormatting.RESET + level + " " + levelTitle), x, y, genericTextCoulor);
        y += 12;
        drawCenteredString(
                fontRendererObj,
                (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:experience") + EnumChatFormatting.RESET 
                		+ xp + StatCollector.translateToLocal("translation.PetBat:missing_xp") + xpToNext), x, y, genericTextCoulor);
        y += 12;
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:health")
                + EnumChatFormatting.RESET + health + " / " + maxHealth), x, y, genericTextCoulor);
        y += 12;
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:attack_power")
                + EnumChatFormatting.RESET + attackStrength), x, y, genericTextCoulor);

        ///////////
        //// Attack stat
        y = drawProgressBar(x, 
        		y, 
        		"Attack bonus progress:", 
        		genericTextCoulor, 
        		0xFFFFFF, 
        		0x0071c2, 
        		0xe5b0ff, 
        		0x0071c2, 
        		0xe5b0ff);
        ////
        ///////////
        y += 30;
        drawCenteredString(fontRendererObj, EnumChatFormatting.ITALIC + levelDesc, x, y, 0xC82536);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2), 0.0F, 50.0F);
        float var4 = 93.75F;
        GL11.glScalef(-var4, -var4, -var4);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glPopMatrix();
        textfield.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }
    
    private void drawMyRect(int x1, int y1, int x2, int y2, int color, float alfa) {
        int j1;

        if (x1 < x2) {
            j1 = x1;
            x1 = x2;
            x2 = j1;
        }

        if (y1 < y2) {
            j1 = y1;
            y1 = y2;
            y2 = j1;
        }

        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(red, green, blue, alfa);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)x1, (double)y2, 0.0D);
        tessellator.addVertex((double)x2, (double)y2, 0.0D);
        tessellator.addVertex((double)x2, (double)y1, 0.0D);
        tessellator.addVertex((double)x1, (double)y1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
    
    private int drawProgressBar(int x, int y, String progressBarText, int progressBarTextCoulor, int progressBGCoulor, int progressCoulor1, int progressCoulor2, int leftTextCoulor, int rightTextCoulor) {
        y += 20;
        int startRect = this.width / 3;
        int endRect = (this.width * 2) / 3;
        int atkIncEnd = (int) ((endRect - startRect) * (Integer.valueOf(attackIncCount).floatValue() / Integer.valueOf(attackIncCountMax).floatValue()));
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + progressBarText), x, y, progressBarTextCoulor);
        y += 12;
        // background rect
        drawMyRect(startRect, y, endRect, y + PROGRESS_BAR_BG_HEIGHT, progressBGCoulor, 0.8f);
        // progress rect
        int endRect2 = Math.max(startRect + atkIncEnd - PROGRESS_BAR_INNER_BORDER, startRect + PROGRESS_BAR_INNER_BORDER + 3);
        drawMyGradientRect(startRect + PROGRESS_BAR_INNER_BORDER, 
        		y + PROGRESS_BAR_INNER_BORDER, 
        		endRect2, 
        		y + PROGRESS_BAR_BG_HEIGHT - PROGRESS_BAR_INNER_BORDER, 
        		progressCoulor1, progressCoulor2, 0.8f, 0.3f, GradientOrientation.HORIZONTAL);
        // current items given, before the progress bar
        String attackItemCountString = "" + attackIncCount;
        int itemCountXOffset = attackItemCountString.length() + 2;
        int textY = y + (PROGRESS_BAR_BG_HEIGHT/ 2) - 3;
        drawString(fontRendererObj, 
        		EnumChatFormatting.BOLD + attackItemCountString, 
        		startRect - PROGRESS_BAR_OUTER_BORDER - itemCountXOffset, 
        		textY, 
        		leftTextCoulor);
        // items needed, after the progress bar 
        drawString(fontRendererObj, 
        		EnumChatFormatting.BOLD + "" + attackIncCountMax, 
        		endRect + PROGRESS_BAR_OUTER_BORDER + 1, 
        		textY, 
        		rightTextCoulor);
        
        return y;
    }
    
    private void drawMyGradientRect(int x1, int y1, int x2, int y2, int color1, int color2, float alfa1, float alfa2, GradientOrientation orientation) {
        float red1 = (float)(color1 >> 16 & 255) / 255.0F;
        float green1 = (float)(color1 >> 8 & 255) / 255.0F;
        float blue1 = (float)(color1 & 255) / 255.0F;
        float red2 = (float)(color2 >> 16 & 255) / 255.0F;
        float green2 = (float)(color2 >> 8 & 255) / 255.0F;
        float blue2 = (float)(color2 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        
        switch(orientation) {
	        case HORIZONTAL:
	        	tessellator.setColorRGBA_F(red1, green1, blue1, alfa1);
	            tessellator.addVertex((double)x1, (double)y1, (double)this.zLevel);
	            tessellator.addVertex((double)x1, (double)y2, (double)this.zLevel);
	            tessellator.setColorRGBA_F(red2, green2, blue2, alfa2);
	            tessellator.addVertex((double)x2, (double)y2, (double)this.zLevel);
	            tessellator.addVertex((double)x2, (double)y1, (double)this.zLevel);
	            break;
	        default:
	          tessellator.setColorRGBA_F(red1, green1, blue1, alfa1);
	          tessellator.addVertex((double)x2, (double)y1, (double)this.zLevel);
	          tessellator.addVertex((double)x1, (double)y1, (double)this.zLevel);
	          tessellator.setColorRGBA_F(red2, green2, blue2, alfa2);
	          tessellator.addVertex((double)x1, (double)y2, (double)this.zLevel);
	          tessellator.addVertex((double)x2, (double)y2, (double)this.zLevel);
	          break;
        }
        
        
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
