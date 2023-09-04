package center.jhub.petbat.common.batAI;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import center.jhub.petbat.common.EntityPetBat;

public class PetBatAIOwnerAttacked extends EntityAITarget {
    private EntityPetBat batEnt;
    private EntityLivingBase theOwnerAttacker;
    
    public PetBatAIOwnerAttacked(EntityPetBat bat) {
        super(bat, false);
        batEnt = bat;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (batEnt.getOwnerEntity() != null) {
            theOwnerAttacker = batEnt.getOwnerEntity().getAITarget();
            return theOwnerAttacker != batEnt.getOwnerEntity() && isSuitableTarget(theOwnerAttacker, false);
        }
        
        return false;
    }
    
    @Override
    public void startExecuting() {
        taskOwner.setAttackTarget(theOwnerAttacker);
        super.startExecuting();
    }
}
