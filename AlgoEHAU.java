package ca.pfv.spmf.algorithms.frequentpatterns.EHAU;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "EHAU" algorithm for High-Average-Utility Itemsets Mining
 * 
 * 
 */
public class AlgoEHAU {
	
	/** the time at which the algorithm started */
	public long startTimestamp = 0;  
	public double  Memory=0;
	/** the time at which the algorithm ended */
	public long endTimestamp = 0; 
	
	/** the number of high-average-utility itemsets generated */
	public int hauiCount =0; 
	
	/** the number of candidate high-average-utility itemsets */
	public int candidateCount =0;
	
	
	private int[] AUUB;   

	// Key : item    Value :  utility list associated to that item
	Map<Integer, UtilityList> mapItemToUtilityList = new HashMap<Integer, UtilityList>();
			
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	
	/** variable for debug mode */
	boolean DEBUG = false;
	
	int tempItems[]=new int[2000];
	int tempUtilities[]=new int[2000];
	int newtempUtilities[]=new int[2000];
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	private int transNum;
	
	
	/**
	 * Default constructor
	 */
	public AlgoEHAU() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		
		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;

		// read the input file
		Dataset dataset = new Dataset(input);
		
		calculateAUUB(dataset);
				
		// to remove unpromising items
    	for(int i=0; i< dataset.getTransactions().size();i++)
    	{
    		// Get the transaction
    		Transaction transaction  = dataset.getTransactions().get(i);
    		transaction.removeUnpromisingItems(AUUB, minUtility,tempItems,tempUtilities);
    	}

	    transNum=dataset.transactions.size();
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH AUUB  >= MIN_UTILITY.
		
	    List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		
		// For each item
		for(Integer item=0;item<=dataset.getMaxItem();++item ){
			if (AUUB[item]>minUtility){
				// create an empty Utility List that we will fill later.
				UtilityList uList = new UtilityList(item);
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high AUUB items
				listOfUtilityLists.add(uList);
			}
			}
		
    	for(int tid=0; tid< dataset.getTransactions().size();tid++){
    		Transaction transaction  = dataset.getTransactions().get(tid);

    		for(int i = 0; i< transaction.items.length; i++){
    			int item =  transaction.items[i];
    
    			// get the utility list of this item
    			UtilityList utilityListOfItem = mapItemToUtilityList.get(item);
    			if (utilityListOfItem != null)
    			{
    				/*  int U[]=transaction.utilities;
    				  int u=(i<U.length-1)? U[i]-U[i+1]:U[i];*/
    				  int u=transaction.utilities[i];
    				  utilityListOfItem.sumUtil+=u;
    				 // utilityListOfItem.sumRUtil+=(i<U.length-1)? U[i+1]:0;;
    				  
    				  double mau=u;
    			      if (mau<transaction.transactionMUtility){
    			    	  mau=(u+transaction.transactionMUtility*(transaction.utilities.length-i-1))/(1+transaction.utilities.length-i-1);
    			      }
    			      utilityListOfItem.sumRAvUtil+=mau;
    				//  utilityListOfItem.sumRAvUtil+=U[i]/U.length-1;
    				
    			utilityListOfItem.addElement(new Element(tid,u,i));
    			}
    			}
    		transaction.items=null;
    		}

		
		// SORT THE LIST OF HIGH AUUB ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityList>(){
			public int compare(UtilityList o1, UtilityList o2) {
				// compare the TWU of the items
				return compareItems(o1.itemset.get(0), o2.itemset.get(0));
			}
			} );
		
		System.gc();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		EHAU( listOfUtilityLists, minUtility,dataset);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		Memory= MemoryLogger.getInstance().getMaxMemory() ;
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Method to compute AUUB
	 * @param dataset a dataset
	 */
	
	
	private void calculateAUUB(Dataset dataset){
		
		// Initialize AUUB for all items
		AUUB = new int[dataset.getMaxItem() + 1];

		// Scan the database to fill the utility bins
		// For each transaction
		for (Transaction transaction : dataset.getTransactions()) {
			// for each item
		//	System.out.println();
			for(Integer item: transaction.getItems()) {
				// we add the transaction utility to the utility bin of the item
				AUUB[item] += transaction.transactionMUtility;
			}
		}

	}
	
	/**
	 * Method to compare items by their AUUB
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {
		int compare = (int)(AUUB[item1] - AUUB[item2]);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}

		/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void EHAU( List<UtilityList> ULs, int minUtility,Dataset dataset)
			throws IOException {
		if ( System.currentTimeMillis()-startTimestamp>600000)
		{
			Memory=-1;
			return;
		}
		if ( MemoryLogger.getInstance().getMaxMemory() >2000)
		{
			Memory=-1;
			return;
		}
		MemoryLogger.getInstance().checkMemory();

		int U=0;
		double AU=0,MAU;
		for(int i=0; i< ULs.size(); i++){
			UtilityList X = ULs.get(i);
            
            MAU= calculateMAU(X,dataset,minUtility);
            if (MAU>=minUtility){
            	List<UtilityList> newULs=new ArrayList<UtilityList>();
            	for (int j=i+1;j<ULs.size();++j){
        			UtilityList Y = ULs.get(j);
        			UtilityList XY=combine(X,Y,dataset);
        			candidateCount++;
        			if (XY.elements.size()>0)
        			  newULs.add(XY);
        			}
            	
            	EHAU(newULs, minUtility, dataset);   				
            }
		}
	//	MemoryLogger.getInstance().checkMemory();
	}
	/**
	 * This method combine two utility Lists  x and y
	 * @param X :  the utility list x
	 * @param y :  the utility list y
	 * @return the combination of X and Y
	 */
private UtilityList combine(UtilityList X,UtilityList Y,Dataset dataset){
	UtilityList XY= new UtilityList(X.itemset);
	XY.addItem(Y.itemset.get(Y.itemset.size()-1));
	int k=0;int iutil=0,mu=0;
	Element ex,ey;
	int j=0;
	Boolean sw=true;
	for(int i=0;i<X.elements.size()&&sw;++i){
		ex=X.elements.get(i);
		ey=Y.elements.get(j);
		while (ex.tid>ey.tid){
		j++;
		if(j==Y.elements.size()){
			sw=false;
			break;
		}
		ey=Y.elements.get(j);
		}
		if (ex.tid==ey.tid){
		  Transaction t= dataset.getTransactions().get(ey.tid);	
		 // int U[]=t.utilities;
		  //int u=(ey.index<U.length-1)? U[ey.index]-U[ey.index+1]:U[ey.index];
		  int u=t.utilities[ey.index];
	      iutil=ex.iutils+u;
	      XY.sumUtil+=iutil;;
	      //XY.sumRUtil+=iutil+((ey.index<U.length-1)?U[ey.index+1]:0);
	      double mau=iutil/XY.itemset.size();
	      if (mau<=t.transactionMUtility){
	    	  mau=(iutil+t.transactionMUtility*(t.utilities.length-ey.index-1))/(XY.itemset.size()+t.utilities.length-ey.index-1);
	      }
	     XY.sumRAvUtil+=mau;
	     // XY.sumRAvUtil+=( ex.iutils+U[ey.index] )/( X.itemset.size()+U.length-ey.index);
		 // iutil=ex.iutils+dataset.getTransactions().get(ey.tid).utilities[ey.index];
		  Element eXY=new Element(ex.tid,iutil,ey.index);
		  XY.addElement(eXY);
		  }
		}
	return XY;
	
}
		


/**
 * This method calculate the Average utility of x
 * @param X :  the utility list
 * @return the Average utility of X
 */
	
private double calculateMAU(UtilityList X,Dataset dataset,int minUtility){
	double MAU=0;
	double AU=0;
	int Util=0;
	int k=0,i=0;
	boolean sw=false;
	int mn=0;
	double MUtil=0;
	
	AU=X.sumUtil/X.itemset.size();
	if (AU>=minUtility){
		try {
			writeOut(X.itemset, AU);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	if (X.sumRAvUtil<minUtility){
		return 0;
	}
	if (AU>=minUtility){
		return AU;
	}
	
	// if not prune yet
	
	for (Element e: X.elements){
			Integer tid=e.tid;
			Transaction t=dataset.transactions.get(tid);
			Util=e.iutils;
			MUtil=Util;
			k=X.itemset.size();
			int len=0;
			i=e.index+1;
			while(i<t.utilities.length){
			   int u=t.utilities[i];
				if (Util*1.0/k < u)
					tempUtilities[len++]=u;
			   //  len=sortedInsert(tempUtilities,len,u);
			   i++;
			}//while	
			mn=0;
			int x=0;
			if (len!= 0){
			Arrays.sort(tempUtilities,0,len-1);
			for (mn=len-1; mn>=0; --mn){
					if (((tempUtilities[mn]+MUtil)*1.0/(x+k+1))>(MUtil)*1.0/(x+k) ){	
						MUtil+=tempUtilities[mn];
						x++;}
					else
						break;
				}
			}
			//MUtil+=Util;
			MUtil/=(k+x);
			MAU+=MUtil;
			if (MAU>=minUtility) 
				return MAU;
		}
return MAU;
}
	
	/**
	/**
	 * Method to write a high average utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(List<Integer> itemset, double AverageUtility) throws IOException {
		hauiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append items
		for (int i = 0; i < itemset.size(); i++) {
			buffer.append(itemset.get(i));
			buffer.append(' ');
		}
		buffer.append(AverageUtility);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Implementation of Insertion sort for sorting a list of items by increasing order of AUUB.
	 * This has an average performance of O(n log n)
	 * @param items list of integers to be sorted
	 * @param items list the utility-bin array indicating the TWU of each item.
	 */
	public static void insertionSort(List<Integer> items, int [] AUUB){
		// the following lines are simply a modified an insertion sort
		
		for(int j=1; j< items.size(); j++){
			Integer itemJ = items.get(j);
			int i = j - 1;
			Integer itemI = items.get(i);
			
			// we compare the AUUB of items i and j
			double comparison = AUUB[itemI] - AUUB[itemJ];
			// if the AUUB is equal, we use the lexicographical order to decide whether i is greater
			// than j or not.
			if(comparison == 0){
				comparison = itemI - itemJ;
			}
			
			while(comparison > 0){
				items.set(i+1, itemI);

				i--;
				if(i<0){
					break;
				}
				
				itemI = items.get(i);
				comparison = AUUB[itemI] - AUUB[itemJ];
				// if the twu is equal, we use the lexicographical order to decide whether i is greater
				// than j or not.
				if(comparison == 0){
					comparison = itemI - itemJ;
				}
			}
			items.set(i+1,itemJ);
		}
	}
    

	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============   EHAU ALGORITHM - SPMF 0.97e - STATS =============");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory()  + " MB");
		System.out.println(" High-utility itemsets count : " + hauiCount); 
		System.out.println(" Candidate count : "             + candidateCount);
		
		System.out.println("===================================================");
	}
	
	/**
	 * Get the size of a Java object (for debugging purposes)
	 * @param object the object
	 * @return the size in MB
	 * @throws IOException
	 */
    private double getObjectSize(
            Object object)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        double maxMemory = baos.size() / 1024d / 1024d;
        return maxMemory;
    }
    
	private int sortedInsert(int[] a,int nElems,int item ){
		if (nElems==0) {
			a[0] = item;
		    return 1;
		}
			
		int j = 0;
		
	    while (j <nElems  && item < a[j]){ 
	    	j++;
	    }
	  int k=nElems;
	  while (k>j){	
	      a[k] = a[k - 1];
	      k--;
	  }
	   
	  a[j] = item;
	  nElems++;
	  return nElems; 
		}


   

}