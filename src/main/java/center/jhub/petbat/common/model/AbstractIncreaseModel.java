package center.jhub.petbat.common.model;

import net.minecraft.entity.DataWatcher;

public abstract class AbstractIncreaseModel {
	private int previousStat;
	private int statAdded;
	private int currentStat;
	private int previousStatItemCount;
	private int statItemCountAdded;
	private int currentStatItemCount;
	private String statName;
	
	public AbstractIncreaseModel(int initialStat, int statItemsGiven, int initialStatItemGivenCount, int neededItemsForStatIncrease, DataWatcher dataWatcher, int dataWatcherStatId, int dataWatcherStatItemId, String statName) {
		calculate(initialStat, statItemsGiven, initialStatItemGivenCount, neededItemsForStatIncrease, dataWatcher, dataWatcherStatId, dataWatcherStatItemId);
		this.statName = statName;
	}
	
	private void calculate(int initialStat, int statItemsGiven, int initialStatItemGivenCount, int neededItemsForStatIncrease, DataWatcher dataWatcher, int dataWatcherStatId, int dataWatcherStatItemId) {
		this.previousStat = initialStat;
		this.previousStatItemCount = initialStatItemGivenCount;
		this.statItemCountAdded = statItemsGiven;
		
		int finalStatItemGivenCount = initialStatItemGivenCount + statItemsGiven;
    	int endStatItemGivenCount = finalStatItemGivenCount;
    	
    	if (finalStatItemGivenCount >= neededItemsForStatIncrease) {
    		int statToIncrease = (int) Math.floor(finalStatItemGivenCount / neededItemsForStatIncrease);
    		
    		this.statAdded = statToIncrease;
    		
    		endStatItemGivenCount = finalStatItemGivenCount % neededItemsForStatIncrease;
    		int endStat = initialStat + statToIncrease;
    		dataWatcher.updateObject(dataWatcherStatId, endStat);
    		
    		this.currentStat = endStat;
    	}
    	
    	this.currentStatItemCount = endStatItemGivenCount;
    	
    	dataWatcher.updateObject(dataWatcherStatItemId, endStatItemGivenCount);
	}
	
	protected void setPreviousStat(int previousStat) {
		this.previousStat = previousStat;
	}

	protected void setStatAdded(int statAdded) {
		this.statAdded = statAdded;
	}

	protected void setCurrentStat(int currentStat) {
		this.currentStat = currentStat;
	}

	protected void setPreviousStatItemCount(int previousStatItemCount) {
		this.previousStatItemCount = previousStatItemCount;
	}

	protected void setStatItemCountAdded(int statItemCountAdded) {
		this.statItemCountAdded = statItemCountAdded;
	}

	protected void setCurrentStatItemCount(int currentStatItemCount) {
		this.currentStatItemCount = currentStatItemCount;
	}

	public int getPreviousStat() {
		return previousStat;
	}

	public int getStatAdded() {
		return statAdded;
	}

	public int getCurrentStat() {
		return currentStat;
	}

	public int getPreviousStatItemCount() {
		return previousStatItemCount;
	}

	public int getStatItemCountAdded() {
		return statItemCountAdded;
	}

	public int getCurrentStatItemCount() {
		return currentStatItemCount;
	}
	
	public String getStatName() {
		return statName;
	}
	
	protected boolean didStatIncrease() {
		return currentStat != previousStat;
	}
}
