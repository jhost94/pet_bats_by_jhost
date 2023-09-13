package center.jhub.petbat.common.model;

import net.minecraft.entity.DataWatcher;

public class AttackIncreaseModel {
	private int previousAttack;
	private int attackAdded;
	private int currentAttack;
	private int previousAttackItemCount;
	private int attackAttackItemCountAdded;
	private int currentAttackItemCount;
	
	public AttackIncreaseModel(int initialAttack, int atkItemsGiven, int initialAttackItemGivenCount, int neededItemsForAttackIncrease, DataWatcher dataWatcher, int dataWatcherAttackId, int dataWatcherAttackItemId) {
		calculate(initialAttack, atkItemsGiven, initialAttackItemGivenCount, neededItemsForAttackIncrease, dataWatcher, dataWatcherAttackId, dataWatcherAttackItemId);
	}
	
	private void calculate(int initialAttack, int atkItemsGiven, int initialAttackItemGivenCount, int neededItemsForAttackIncrease, DataWatcher dataWatcher, int dataWatcherAttackId, int dataWatcherAttackItemId) {
		this.previousAttack = initialAttack;
		this.previousAttackItemCount = initialAttackItemGivenCount;
		this.attackAttackItemCountAdded = atkItemsGiven;
		
		int finalAttackItemGivenCount = initialAttackItemGivenCount + atkItemsGiven;
    	int endAttackItemGivenCount = finalAttackItemGivenCount;
    	
    	if (finalAttackItemGivenCount >= neededItemsForAttackIncrease) {
    		int dmgToIncrease = (int) Math.floor(finalAttackItemGivenCount / neededItemsForAttackIncrease);
    		
    		this.attackAdded = dmgToIncrease;
    		
    		endAttackItemGivenCount = finalAttackItemGivenCount % neededItemsForAttackIncrease;
    		int endAttack = initialAttack + dmgToIncrease;
    		dataWatcher.updateObject(dataWatcherAttackId, endAttack);
    		
    		this.currentAttack = endAttack;
    	}
    	
    	this.currentAttackItemCount = endAttackItemGivenCount;
    	
    	dataWatcher.updateObject(dataWatcherAttackItemId, endAttackItemGivenCount);
	}

	public int getPreviousAttack() {
		return previousAttack;
	}

	public int getAttackAdded() {
		return attackAdded;
	}

	public int getCurrentAttack() {
		return currentAttack;
	}

	public int getPreviousAttackItemCount() {
		return previousAttackItemCount;
	}

	public int getAttackAttackItemCountAdded() {
		return attackAttackItemCountAdded;
	}

	public int getCurrentAttackItemCount() {
		return currentAttackItemCount;
	}
}
