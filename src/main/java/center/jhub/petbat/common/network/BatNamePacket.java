package center.jhub.petbat.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import center.jhub.petbat.common.ItemPocketedPetBat;
import center.jhub.petbat.common.PetBatMod;
import center.jhub.petbat.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;

public class BatNamePacket implements IPacket {

    private String user;
    private String batName;

    public BatNamePacket() {
    }

    public BatNamePacket(String bdata, String idata) {
        user = bdata;
        batName = idata;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes) {
        ByteBufUtils.writeUTF8String(bytes, user);
        ByteBufUtils.writeUTF8String(bytes, batName);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes) {
        user = ByteBufUtils.readUTF8String(bytes);
        batName = ByteBufUtils.readUTF8String(bytes);
        EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().func_152612_a(user);
        if (p != null) {
            if (p.getCurrentEquippedItem() != null && p.getCurrentEquippedItem().getItem() == PetBatMod.instance().itemPocketedBat) {
                ItemPocketedPetBat.writeBatNameToItemStack(p.getCurrentEquippedItem(), batName);
            }
        }
    }

}
