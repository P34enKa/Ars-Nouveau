package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.api.util.NBTUtil;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketANEffect;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

public class ArcaneRelayTile extends AbstractManaTile{

    public ArcaneRelayTile() {
        super(BlockRegistry.ARCANE_RELAY_TILE);
    }
    public ArcaneRelayTile(TileEntityType<?> type){
        super(type);
    }

    private BlockPos toPos;
    private BlockPos fromPos;


    public boolean setTakeFrom(BlockPos pos){
        if(BlockUtil.distanceFrom(pos, this.pos) > 10){
            return false;
        }
        this.fromPos = pos;
        update();
        return true;
    }

    public boolean setSendTo(BlockPos pos ){
        if(BlockUtil.distanceFrom(pos, this.pos) > 10){
            return false;
        }
        this.toPos = pos;
        update();
        return true;
    }

    public void clearPos(){
        this.toPos = null;
        this.fromPos = null;
          update();
    }

    @Override
    public int getTransferRate() {
        return 100;
    }

    @Override
    public int getMaxMana() {
        return 200;
    }

    public boolean closeEnough(BlockPos pos, int distance){
        return BlockUtil.distanceFrom(pos, this.pos) <= distance;
    }

    @Override
    public void tick() {
        if(world.isRemote){
            return;
//            System.out.println(this.fromPos);
        }

        if(world.getGameTime() % 20 != 0 || toPos == null)
            return;

        if(fromPos != null){
            // Block has been removed
            if(!(world.getTileEntity(fromPos) instanceof AbstractManaTile)){
                fromPos = null;
                world.notifyBlockUpdate(this.pos, world.getBlockState(pos),  world.getBlockState(pos), 2);
                return;
            }else if(world.getTileEntity(fromPos) instanceof AbstractManaTile){
                // Transfer mana fromPos to this
                AbstractManaTile fromTile = (AbstractManaTile) world.getTileEntity(fromPos);
                if(fromTile.getCurrentMana() >= this.getTransferRate() && this.getCurrentMana() + this.getTransferRate() <= this.getMaxMana()){
                    fromTile.removeMana(this.getTransferRate());
                    this.addMana(this.getTransferRate());
                    Networking.sendToNearby(world, pos, new PacketANEffect(PacketANEffect.EffectType.TIMED_GLOW,pos.getX(), pos.getY(), pos.getZ(),
                            fromPos.getX(), fromPos.getY(), fromPos.getZ(), 5));
                }
            }
        }
        if(!(world.getTileEntity(toPos) instanceof AbstractManaTile)){
            toPos = null;
            update();

            return;
        }
        AbstractManaTile toTile = (AbstractManaTile) this.world.getTileEntity(toPos);
        if(this.getCurrentMana() >= this.getTransferRate() && toTile.getCurrentMana() + this.getTransferRate() <= toTile.getMaxMana()){
            this.removeMana(this.getTransferRate());
            toTile.addMana(this.getTransferRate());
            Networking.sendToNearby(world, pos, new PacketANEffect(PacketANEffect.EffectType.TIMED_GLOW,  toPos.getX(), toPos.getY(), toPos.getZ(),pos.getX(), pos.getY(), pos.getZ(), 5));
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        if(NBTUtil.hasBlockPos(tag, "to")){
            this.toPos = NBTUtil.getBlockPos(tag, "to");
        }
        if(NBTUtil.hasBlockPos(tag, "from")){
            this.fromPos = NBTUtil.getBlockPos(tag, "from");
        }
        super.read(state, tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        if(toPos != null)
            NBTUtil.storeBlockPos(tag, "to", toPos);
        if(fromPos != null)
            NBTUtil.storeBlockPos(tag, "from", fromPos);
        return super.write(tag);
    }

}