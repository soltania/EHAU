package ca.pfv.spmf.algorithms.frequentpatterns.EHAU;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUIMiner;

public class MainTestEHAU {

	public static void main(String [] arg) throws IOException{

		String dir="D:/workspace/datasets/Synthetic/T/T10I50DXK/";
		String input=dir+"testutil100.txt";
		String output = ".//output.txt";
    	int min_utility =(int)(68107224*0.01);  //
    	//int min_utility =1000;  //

		
		// Applying the MHAI algorithm
		AlgoEHAU EHAU= new AlgoEHAU();
		EHAU.runAlgorithm(input, output, min_utility);
		System.out.println("minUtil: "+min_utility);
		EHAU.printStats();

	}


}
