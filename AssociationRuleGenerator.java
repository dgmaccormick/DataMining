/*
 * Program to generate Association rules using frequent item sets in a database of transactions.
 * 
 * @Author David MacCormick, February 2014
 */

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;

public class AssociationRuleGenerator {

	double minSupport, minConfidence;
	
	private String[] attributeNames; // list of attributes
	private int numAttributes; // the number of attributes in the file
	private int numRows;
	private ArrayList<String[]> itemSet; // list to hold all of the tuples
	
	private HashMap<Integer, String> itemMap;
	private HashMap<Integer, String> attrMap; //maps item keys to attribute names
	
	private int[][] itemSetNumbers;
	private Set[] itemSetList;
	
	private ArrayList<Set<Integer>> completeSetList;
	HashMap<Set<Integer>, Double> suppRates;
    public AssociationRuleGenerator() throws IOException
    {
    	completeSetList = new ArrayList<Set<Integer>>(); // list of all frequent itemsets
    	//list to hold corresponding supp rate of each frequent itemset
    	suppRates = new HashMap<Set<Integer>, Double>();
    	
    	readFile();
    	
    	System.out.print("Choose a minimum support rate (0.0 to 1.0): ");
    	Scanner input = new Scanner(System.in);
    	minSupport = input.nextDouble();    	
    	System.out.print("Choose a minimum confidence rate (0.0 to 1.0): ");
    	minConfidence = input.nextDouble();
    	    	
    	getInitialItemsets();

    	generateRules(completeSetList); // generate the association rules
    }

    /*
     * method to store contents of database into
     */
    private void readFile()
    {
    	System.out.print("Enter a file name: ");
    	Scanner input = new Scanner(System.in);
    	String filename = input.next();

    	BufferedReader br;
    	try {
    		br = new BufferedReader(new FileReader(filename));
    		String currentLine = br.readLine();
    		
    		StringTokenizer st = new StringTokenizer(currentLine);
    		
    		numAttributes = st.countTokens();
    		
    		attributeNames = new String[numAttributes];
    		for(int i = 0; i < numAttributes; i++){
    			attributeNames[i] = st.nextToken();
    			//System.out.println(attributeNames[i]);
    		}
    		
    		// create itemSet, an ArrayList of arrays (each representing a row from the database)
    		itemSet = new ArrayList<String[]>();
    		 	
    		// place data into itemSet
    		while((currentLine = br.readLine()) != null)
    		{					
    			st = new StringTokenizer(currentLine);

    			if(!st.hasMoreTokens()) break;
    			
    			String[]items = new String[numAttributes];
    			String curr;
        		for(int i = 0; i < numAttributes; i++){
        			if(st.hasMoreTokens()){
        				curr = st.nextToken();	
	        			items[i] = curr;
        			}
        		}
    			
    			itemSet.add(items);   			
    		} 	
    		numRows = itemSet.size();
    		
    	} catch(IOException e){
    		System.out.println("Error reading file.");
    		System.exit(0);
    	}
    } // end of readFile method
    
    private void getInitialItemsets()
    {
    	attrMap = new HashMap<Integer, String>();
    	itemMap = new HashMap<Integer, String>();    	
    	HashMap[] I = new HashMap[numAttributes];    	  
    	int index = 0;   
    	    	
    	itemSetNumbers = new int[numRows][numAttributes];
    	itemSetList = new HashSet[numRows];
    	//initial the array of Sets
    	for(int j = 0; j < numRows; j++){
    		Set<Integer> row = new HashSet<Integer>();
    		itemSetList[j] = row;
    	}
    	
    	for(int i = 0; i < numAttributes; i++)
    	{
    		I[i] = new HashMap<String, Integer>();
    		for(int j = 0; j < numRows; j++)
    		{
    			String[] curr = itemSet.get(j);   			  			
    			
    			if(!I[i].containsKey(curr[i]))
    			{   	   				
    				I[i].put(curr[i], index);   				
    				itemMap.put(index, curr[i]);
    				
    				attrMap.put(index, attributeNames[i]);
    				index++;   				
    			} 
    			
				itemSetNumbers[j][i] = (Integer)I[i].get(curr[i]);	
				itemSetList[j].add((Integer)I[i].get(curr[i]));
    		}		
    	} 

    	
    	// store the initial itemsets in an ArrayList
    	ArrayList<Set<Integer>> C = new ArrayList<Set<Integer>>();   	
    	for(int i = 0; i < itemMap.size(); i++)
    	{
    		Set<Integer> curr = new HashSet<Integer>();
    		curr.add(i);
    		C.add(curr);
    	}
    	
    	
    	scanFrequencies(C, 1);
    }
    
    /*
     * method to scan candidate itemsets for frequencies and store the frequent itemsets in L
     */
    private void scanFrequencies(ArrayList<Set<Integer>> C, int size) 
    {
    	ArrayList<Set<Integer>> L = new ArrayList<Set<Integer>>();
    	
    	ArrayList<Double> supportRates = new ArrayList<Double>();
    	
    	// calculate the required frequency based on the minimum support rate
    	int minFreq = (int)(minSupport * numRows);
    	double minRemainder = (minSupport * numRows) - minFreq;
    	if(minRemainder > 0)
    		minFreq++;
    	
    	// for each candidate itemset, add to L if it is a frequent itemset
    	for(Set<Integer> curr : C)
    	{
    		int freq = 0; // keep track of the frequency of this item set
    		    		
    		// check which tuple contain this itemset	
    		for(int j = 0; j < numRows; j++)
    		{					
    			if(itemSetList[j].containsAll(curr)){
    				freq++;
    			}    										
    		}
    		
    		
    		// add item sets to L if they have the required frequency
    		double supp = (double)freq / (double)numRows;
    		
    		//if(freq >= minFreq){
    		if(supp >= minSupport){	
    			L.add(curr);	
    			//double supp = (double)freq / (double)numRows;
    			suppRates.put(curr, supp);
    		} 
	
    	}    	
    	completeSetList.addAll(L);
    	
    	// continue only if C contains frequent item sets
    	if(L.size() > 0)
    		generateCandidates(L, size+1);   	
    	
    }
    
    /*
     * method to generate candidate item sets
     */
    private void generateCandidates(ArrayList<Set<Integer>> L, int size)
    {
    	//candidate item set to be generated
    	ArrayList<Set<Integer>> C = new ArrayList<Set<Integer>>();
    	
    	
    	ArrayList<Integer>newSet = new ArrayList<Integer>();
    	
    	
    	// combine all elements into one big set
    	for(int i = 0; i < L.size(); i++)
    	{
    		Set<Integer>set1 = L.get(i);
    		for(int num : set1){
    			if(!newSet.contains(num))
    				newSet.add(num);
    		}
    	}

    	C = getSubsets(newSet, size);
    	
    	scanFrequencies(C, size);
    }
    
    /*
     * This method is based on a method found on stackoverflow.com
     * http://stackoverflow.com/questions/12548312/find-all-subsets-of-length-k-in-an-array, retrieved February 27, 2014.
     * 
     * The method recursively finds the subsets of length k of a given superset, and stores them in a list
     */
    private static void getSubsets(ArrayList<Integer> superSet, int k, int idx, Set<Integer> current,List<Set<Integer>> solution) 
    {
        //successful stop clause
        if (current.size() == k) {
            solution.add(new HashSet<>(current));
            return;
        }
        //unseccessful stop clause
        if (idx == superSet.size()) return;
        Integer x = superSet.get(idx);
        current.add(x);
        //"guess" x is in the subset
        getSubsets(superSet, k, idx+1, current, solution);
        current.remove(x);
        //"guess" x is not in the subset
        getSubsets(superSet, k, idx+1, current, solution);
    }
    /*
     * This method is based on a method found on stackoverflow.com
     * http://stackoverflow.com/questions/12548312/find-all-subsets-of-length-k-in-an-array, retrieved February 27, 2014.
     * 
     * The method recursively returns a list of all subsets of length k of the given superset.
     */
    public static ArrayList<Set<Integer>> getSubsets(ArrayList<Integer> superSet, int k) {
        ArrayList<Set<Integer>> res = new ArrayList<>();
        getSubsets(superSet, k, 0, new HashSet<Integer>(), res);
        return res;
    }
    
    /*
     * method to generate the association rules
     */
    private void generateRules(ArrayList<Set<Integer>> list) throws IOException
    {

    	int ruleIndex = 1; //counter to keep track of rule number
    	  	
    	String rules = "";
    	ArrayList<String> allRules = new ArrayList<String>();
    	int i = 0;
    	for(Set<Integer> curr : list)
    	{
    		// only look at sets with k >= 2
    		if(curr.size() > 1)
    		{
    			String format = "%1.2f";
    			
    			String support = String.format(format, suppRates.get(curr));
    			String strConf;   			
    			
    			ArrayList<Integer> superset = new ArrayList<Integer>();
    			superset.addAll(curr);
    			
    			// get the subsets of this set
    			ArrayList<Set<Integer>> subsets = new ArrayList<Set<Integer>>();
    			for(int x = 1; x <= superset.size() -1; x++){
    				subsets.addAll(getSubsets(superset, x));
    			}
    			String attr, val;
    			
    			// for each subset s, generate association rules s => L - s
    			for(Set<Integer> s : subsets)
    			{	   				
    				Set<Integer>diff = new HashSet<Integer>();
    				diff.addAll(curr);
    				diff.removeAll(s);   
    				
    				double diffSupp = 0;
    				if(!suppRates.containsKey(s))
    					System.out.println("Cannot find supp rate");
    				else
    					diffSupp = suppRates.get(s);
    				
    				double conf = suppRates.get(curr) / (double)diffSupp;
    				strConf = String.format(format, conf);
    				
    				if(conf >= minConfidence)
    				{
    					rules = "";
    					rules = rules + "Rule#" + ruleIndex + ": (Support=" + support + ", Confidence=" + strConf + ")\n";
        				rules = rules + "{ ";
        				
        				//System.out.print(rules);
        				for(int num : s)
        				{
        					attr = attrMap.get(num);
        					val = itemMap.get(num);
        					rules = rules + attr + "=" + val + " ";					
        				}
        				rules += " }\n----> { ";
        				for(int num : diff)
        				{
        					attr = attrMap.get(num);
        					val = itemMap.get(num);
        					rules += attr + "=" + val + " ";	
        				}
        				rules += " }\n\n";
        				allRules.add(rules);
        				ruleIndex++;
    				}   				  				
    			}
    		}
    		i++;
    	}
    	
    	BufferedWriter bw = new BufferedWriter(new FileWriter("Rules"));
    	
    	bw.append("Summary:");
    	bw.newLine();
    	bw.append("Total rows in the original set: " + numRows);
    	bw.newLine();
    	bw.append("Total rules discovered: " + allRules.size());
    	bw.newLine();
    	bw.append("The selected measures: Support=" + minSupport + " Confidence=" + minConfidence);
    	bw.newLine();
    	bw.append("----------------------------------------------------------------------");
    	bw.newLine();
    	bw.newLine();
    	bw.append("Rules:");
    	bw.newLine(); bw.newLine();
    	for(String s : allRules)
    	{
    		bw.append(s);
    	}
    	bw.newLine();
    	bw.close();
    }
    
    
    public static void main(String[] args) throws IOException
    {
		new AssociationRuleGenerator();
    }
	
	
	
	
}
