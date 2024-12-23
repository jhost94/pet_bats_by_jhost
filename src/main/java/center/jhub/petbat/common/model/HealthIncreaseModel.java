package center.jhub.petbat.common.model;

import net.minecraft.entity.DataWatcher;

public class HealthIncreaseModel extends AbstractIncreaseModel {
	
	public HealthIncreaseModel(int initialHealth, int healthItemsGiven, int initialHealthItemGivenCount, int neededItemsForHealthIncrease, DataWatcher dataWatcher, int dataWatcherHealthId, int dataWatcherHealthItemId) {
		super(initialHealth, healthItemsGiven, initialHealthItemGivenCount, neededItemsForHealthIncrease, dataWatcher, dataWatcherHealthId, dataWatcherHealthItemId, "max health");
	}

	public int getPreviousHealth() {
		return super.getPreviousStat();
	}

	public int getHealthAdded() {
		return super.getStatAdded();
	}

	public int getCurrentHealth() {
		return super.getCurrentStat();
	}

	public int getPreviousHealthItemCount() {
		return super.getPreviousStatItemCount();
	}

	public int getHealthItemCountAdded() {
		return super.getStatItemCountAdded();
	}

	public int getCurrentHealthItemCount() {
		return super.getCurrentStatItemCount();
	}
	
	public boolean didHealthIncrease() {
		return super.didStatIncrease();
	}
}
