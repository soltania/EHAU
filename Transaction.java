package ca.pfv.spmf.algorithms.frequentpatterns.EHAU;

 

/**
 * This class represents a transaction
* 
 * @author Philippe Fournier-Viger
 */
public class Transaction {
	
		/** an array of items representing the transaction */
    int[] items;
    /** an array of utilities associated to items of the transaction */
    int[] utilities;
    
    /** the transaction utility of the transaction or projected transaction */
     int transactionMUtility; 
     
    
     /**
      * Constructor of a transaction
      * @param items the items in the transaction
      * @param utilities the utilities of item in this transaction
      * @param transactionUtility the transaction utility
      */
    public Transaction(int[] items, int[] utilities, int transactionMUtility) {
    	this.items = items;
    	this.utilities = utilities;
    	this.transactionMUtility = transactionMUtility;
    }
    
    public Transaction() {
    	this.items = null;
    	this.utilities = null;
    	this.transactionMUtility = -1;
    }
    
    
    /**
     * Get a string representation of this transaction
     */
     public String toString() {
		StringBuilder buffer = new StringBuilder();
		 for (int i = 0; i < items.length; i++) {
			 buffer.append(items[i]);
			 buffer.append("[");
			 buffer.append(utilities[i]);
			 buffer.append("] ");
		 }
		 return buffer.toString();
	}
 

     /**
      * Get the array of items in this transaction
      * @return array of items
      */
    public int[] getItems() {
        return items;
    }
    

    /**
     * Get the array of utilities in this transaction
     * @return array of utilities
     */
    public int[] getUtilities() {
        return utilities;
    }

    
    /**
     * get the last position in this transaction
     * @return the last position (the number of items -1 )
     */
    public int getLastPosition(){
    	return items.length -1;
    }

    /**
     * This method removes unpromising items from the transaction and at the same time rename
     * items from old names to new names
     * @param oldNamesToNewNames An array indicating for each old name, the corresponding new name.
     */
	public void removeUnpromisingItems(int AUUB[], int minUtility,int tempItems[], int tempUtilities[]) {
	    	// In this method, we used buffers for temporary storing items and their utilities
			// (tempItems and tempUtilities)
			// This is for memory optimization.
			boolean sw=false;
	    	// for each item
	    	int i = 0;
	    	for(int j=0; j< items.length;j++) {	    		
	    			    		
	    		// if the item is promising 
	    		if(AUUB[items[j]]>=minUtility) {
	    			// copy the item and its utility
	    			tempItems[i] = items[j];
	    			tempUtilities[i] = utilities[j];
	    			i++;
	    		}
	    	}
	    	utilities=new int[i];
	    	items=new int[i];
	    	if (i==0)return;

	    	transactionMUtility=tempUtilities[0];
	    	utilities[0]=tempUtilities[0];
	    			items[0]=tempItems[0];
	    	for(int x = 1; x < i; x++){
	    			utilities[x]=tempUtilities[x];
	    			items[x]=tempItems[x];
	    			if(transactionMUtility <  utilities[x]){
	    				transactionMUtility =  utilities[x];
	    			}
	    		}

	    	// Sort by increasing AUUB values
	    	insertionSort(items, utilities,AUUB);
	    	/* for (int j = items.length-2; j >= 0 ; j--) {
	         	utilities[j]+= utilities[j+1];
	         }*/
		}
		
	/**
	 * Implementation of Insertion sort for integers.
	 * This has an average performance of O(n log n)
	 * @param items array of integers
	 */
	public static void insertionSort(int [] items,  int[] utitilies,int AUUB[]){
		for(int j=1; j< items.length; j++){
			int itemJ = items[j];
			int utilityJ = utitilies[j];
			int AUUBJ=AUUB[itemJ];
			int i = j - 1;
			for(; i>=0 && (AUUB[items[i]]  > AUUBJ); i--){
				items[i+1] = items[i];
				utitilies[i+1] = utitilies[i];
			}
			items[i+1] = itemJ;
			utitilies[i+1] = utilityJ;
		}
	}


}
