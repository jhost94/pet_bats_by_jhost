package center.jhub.petbat.common.model;

import net.minecraft.entity.DataWatcher;

public class AttackIncreaseModel extends AbstractIncreaseModel {
	
	public AttackIncreaseModel(int initialAttack, int atkItemsGiven, int initialAttackItemGivenCount, int neededItemsForAttackIncrease, DataWatcher dataWatcher, int dataWatcherAttackId, int dataWatcherAttackItemId) {
		super(initialAttack, atkItemsGiven, initialAttackItemGivenCount, neededItemsForAttackIncrease, dataWatcher, dataWatcherAttackId, dataWatcherAttackItemId, "attack");
	}

	public int getPreviousAttack() {
		return super.getPreviousStat();
	}

	public int getAttackAdded() {
		return super.getStatAdded();
	}

	public int getCurrentAttack() {
		return super.getCurrentStat();
	}

	public int getPreviousAttackItemCount() {
		return super.getPreviousStatItemCount();
	}

	public int getAttackAttackItemCountAdded() {
		return super.getStatItemCountAdded();
	}

	public int getCurrentAttackItemCount() {
		return super.getCurrentStatItemCount();
	}
}
