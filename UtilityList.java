package ca.pfv.spmf.algorithms.frequentpatterns.EHAU;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;



//import ca.pfv.spmf.algorithms.frequentpatterns.MHAI2.Element;


/**
 * This class represents a UtilityList as used by the HUI-Miner algorithm.
 *
 * @see AlgoHUIMiner
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class UtilityList {
	List<Integer> itemset;  // the item
	List<Element> elements = new ArrayList<Element>();  // the elements
//	int sumRUtil=0;
	double sumRAvUtil=0;
	int sumUtil=0;

	//List<Integer> TIDs = new ArrayList<Integer>();  // the elements
	
	 
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public UtilityList(Integer item){
		this.itemset=new ArrayList<Integer>(1);
		this.itemset.add(item);
	}
	public UtilityList(List<Integer> items){
		this.itemset=new ArrayList<Integer>(items.size());
		this.itemset.addAll(items);
	}
	
	public void addItem(int item){
		this.itemset.add(item);
	}

/*	public void addTransaction(int tid){
		TIDs.add(tid);
	}
	*/
	public void addElement(Element element){
		elements.add(element);
	}
	
}
