/*
 * A program that accepts an input file containing a set of training data,
 * creates a Decision Tree, and makes a list of classification rules based on the training data.
 * Uses the ID3 algorithm to make the decision tree.
 * 
 * @Author David MacCormick, March 2014
 */

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.lang.Math;

public class DTGenerator  {

	
	private ArrayList<String> attributeNames; // list of attributes
	private int numAttributes; // the number of attributes (columns) in the input file
	private int numRows; // the number of tuples
	private ArrayList<String[]> itemSet; // list to hold all of the tuples from the input file
	
	private String targetAttr; // the target attribute, chosen by the user
	private String target1;
	private String target2;
	
	private Tree decisionTree; // the decision tree
	private BufferedWriter bw;
	
	/* 
	 * Constructor
	 */
    public DTGenerator() throws IOException
    {
    	bw = new BufferedWriter(new FileWriter("Rules.txt"));
    	
    	readFile();	
    	   	
    	getBinaryAttributes();
	
    	bw.append("Summary:");
    	bw.newLine();
    	bw.newLine();
    	bw.append("The selected target attribute: " + targetAttr);
    	bw.newLine();
    	bw.append("----------------------------------------------------------------------");
    	bw.newLine();
    	bw.newLine();
    	bw.append("Rules:");
    	bw.newLine(); bw.newLine();
    	
    	parseTree(decisionTree.getRoot(), 0); // add content to the Rules file
    	
    	bw.newLine();
    	bw.close();
    }

    /*
     * method to store the training data in an ArrayList and to remove noise from the training data.
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
    		
    		attributeNames = new ArrayList<String>();
    		for(int i = 0; i < numAttributes; i++){
    			attributeNames.add(st.nextToken());
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
        		
    			// make sure there is not already an equivalent item in the set
        		if(!itemSet.contains(items))
        			itemSet.add(items); 
        		else
        			System.out.println("Noise");
    		} 	
    		numRows = itemSet.size();
    		
    		
    		
    		
    	} catch(IOException e){
    		System.out.println("Error reading file.");
    		System.exit(0);
    	}
    } // end of readFile method
    
    private ArrayList[] I; // an array holding an ArrayList for each column, where each ArrayList contains all of the values in that column
    
    private void getBinaryAttributes()
    {
    	I = new ArrayList[numAttributes];
    	
    	for(int i = 0; i < numAttributes; i++)
    	{
    		I[i] = new ArrayList<String>();
    		
    		for(int k = 0; k < numRows; k++)
    		{
    			String s = itemSet.get(k)[i];
    			if(!I[i].contains(s))
    				I[i].add(s);
    		}   		
    	}
    	
    	ArrayList<String>binaryAttributes = new ArrayList<String>();
    	
    	System.out.println("Choose an attribute (by number): ");
    	int index = 1;
    	
    	for(int i = 0; i < numAttributes; i++)
    	{
    		if(I[i].size() == 2){
    			System.out.println("  " + index + ". " + attributeNames.get(i) );
    			index++;
    			binaryAttributes.add(attributeNames.get(i));
    		}
    	}

    	System.out.print("Attribute: ");
    	Scanner input = new Scanner(System.in);
    	int attrNum = input.nextInt(); 
    	int target = attributeNames.indexOf(binaryAttributes.get(attrNum-1));
    	targetAttr = attributeNames.get(target);
    	System.out.println("Target attribute is: " + targetAttr);
    	System.out.println();
    	System.out.println();
    	
    	target1 = (String)I[target].get(0);
    	target2 = (String)I[target].get(1);
    	   	
    	
    	ArrayList<Integer> attrs = new ArrayList<Integer>();
    	for(int i = 0; i < numAttributes; i++)
    	{
    		if(i != target)
    			attrs.add(i);
    	}
    	
    	makeDecisionTree(itemSet, target, attrs, "root", null);
    	
    	
    }
    
    
    
    /*
     * Method to make the decision tree using ID3 algorithm
     */
    private void makeDecisionTree(ArrayList<String[]> examples, int target, ArrayList<Integer> attributes, String rule, Node parent)
    {
    	
    	Node rootNode = new Node();   	
		
    	// in case this is the first the the method is being called
		if(parent == null)
		{
			rootNode.addAttributes(attributes);
			decisionTree = new Tree(rootNode);
		}
		else
		{
			rootNode.setParent(parent);
			rootNode.addAttributes(parent.getAttributes());		
			parent.addChild(rootNode);
		}			
			
    	rootNode.description = rule;

    	int target1Count = 0;
    	int target2Count = 0;
    	
    	//
    	for(String[] s : examples)
    	{
    		
    		
    		
    		if(s[target].equals(target1))
    			target1Count++;
    		else if(s[target].equals(target2))
    			target2Count++;
    		else
    			System.out.println("error!!!");
    	}
    	
    	if(target1Count == examples.size())
    	{
    		rootNode.label = target1;
    		rootNode.setConfidence(1.0);
    	}
    	else if(target2Count == examples.size())
    	{
    		rootNode.label = target2;
    		rootNode.setConfidence(1.0);
    	}
    	// no more attributes left, so create a leaf node
    	else if(rootNode.getAttributes().isEmpty()) //attributes.isEmpty())
    	{
    		
    		
    		if(target1Count > target2Count){
    			rootNode.label =  rootNode.attr + " is " + target1;
    			rootNode.setConfidence((double)target1Count / (double)(target1Count + target2Count));
    		}
    		else if(target1Count < target2Count){
    			rootNode.label = target2;
    			rootNode.setConfidence((double)target2Count / (double)(target1Count + target2Count));
    		}
    		else {
    			rootNode.label = rootNode.attr + " is " + target1;//"there is a tie. ";
    			rootNode.setConfidence(0.5);
    		}
    	}
 	
    	// Otherwise:
    	else
    	{
    		// get the attribute that results in the most information gain
    		int rootAttr = getRootAttr(examples, target, attributes);
    		rootNode.attr = attributeNames.get(rootAttr);

    		boolean allTheSame = true;
    		String testStr = examples.get(0)[rootAttr];
    		for(String[]s : examples){
    			if(!s[rootAttr].equals(testStr))
    				allTheSame = false;
    		}   		
    		
    		ArrayList<String> values = I[rootAttr];
    		
    		//System.out.println("Node Attributes: " + rootNode.getAttributes().toString() + "  Name: " + rootNode.attr);
    		
    		rootNode.removeAttribute(attributes.indexOf(rootAttr));
    		
    		
    		// for each possible value of the root attribute:
    		for(String s : values)
    		{
    			ArrayList<String[]>list = new ArrayList<String[]>();
    			for(int i = 0; i < examples.size(); i++)
    			{
    				String[]arr = examples.get(i);
    				if(arr[rootAttr].equals(s))
    					list.add(arr);
    			}
    			
    			// if list is empty, then we have a leaf node
    			if(list.isEmpty())
    			{
    				
    				Node child = new Node();
    				
					if(target1Count >= target2Count)
		    			child.label = rootNode.attr + " is a " + target1;
		    		else// if(target1Count < target2Count)
		    			child.label = rootNode.attr + " is a " + target2;
		    		//else
		    		//	child.label = "warning - there is a tie";
    			}
    			else 
    			{
    				
    				makeDecisionTree(list, target, rootNode.getAttributes(), rootNode.attr + " is " + s, rootNode);
    			}
    			
    		}
    	}
    	
    }
    
    /*
     * Depth first search algorithm to parse the tree
     */
    
    private void parseTree(Node curr, int level) throws IOException
    {
  	
    	ArrayList<Node> children = curr.getChildren();
    	
    	//System.out.println("Label: " + curr.label + "    Descr: " + curr.description + "   Attr: " + curr.attr);
    	if(level != 0)
    	{
    		String str = "";
    		for(int i = 0; i < level; i++) 
    			str = str + " ";
    		
    		bw.append(str + "If " + curr.description + ",  then  ");
    		if(curr.isLeaf()){
    			bw.append(targetAttr + " is " + curr.label);
    			bw.append(" (confidence=" + curr.confidence + ")");
    		}
    		bw.newLine();
    		
    	}
    	
    	for(Node n : children)
    	{
    		parseTree(n, level + 1);
    	}
    	
    }
   
    /*
     * method to decide which attribute results in the most information gain.
     */
    private int getRootAttr(ArrayList<String[]> examples, int target, ArrayList<Integer> attributes)
    {
    	double entropy = entropy(examples, target);
    	int rootAttr = attributes.get(0);
    	double informationGain;
    	double maxGain = 0.0;
    	for(int attr: attributes)
    	{
    		double expectedEntropy = expectedEntropy(attr, examples, target);
    		  		
    		informationGain = entropy - expectedEntropy;
    		
    		if(informationGain > maxGain) {
    			maxGain = informationGain;
    			rootAttr = attr;
    		}
    	}
    	//System.out.println("Root Attribute is : " + rootAttr);
    	return rootAttr;
    }
    private double expectedEntropy(int attr, ArrayList<String[]> examples, int target)
    {
    	double exp_entropy = 0.0;
    	
    	ArrayList<String> list = I[attr];  	
    	ArrayList<String[]> examples2 = new ArrayList<String[]>();
		for(String s : list)
		{
			
			examples2.clear();
			for(String[]arr : examples)
			{
				if(arr[attr].equals(s)){
					examples2.add(arr);
					
				}
				
			}
			double p = (double)examples2.size() / (double)examples.size();
			exp_entropy += p * entropy(examples2, target);
			
			//System.out.println("Entropy for " + s+ ": "+ entropy(examples2, target) 
			//		           + " * " + examples2.size() + " / " + examples.size());
		}
		//System.out.println("Exp entropy: " + exp_entropy);
		return exp_entropy;
		
    }     
    
    /*
     * Calculates entropy, to be used in the information gain calculation.
     */
    private double entropy(ArrayList<String[]> examples, int target)
    {
    	double entropy;
    	
    	int t1 = 0, t2 = 0;
    	
    	for(String[] s : examples)
    	{
    		if(s[target].equals(target1))
    			t1++;
    		else if(s[target].equals(target2))
    			t2++;
    		else
    			System.out.println("error!!!");
    	}
    	
    	if(t1 == examples.size() || t2 == examples.size())
    		entropy = 0.0;
    	
    	else {
	    	double p0 = (double)t1 / (double)examples.size();
	    	double p1 = (double)t2 / (double)examples.size();
	    	
	    	entropy = -(p0 * (Math.log(p0)/Math.log(2)) + p1 * (Math.log(p1)/Math.log(2)) );
	    	//System.out.println("Entropy: " + entropy + ", p0: " + p0 + ", p1: " + p1);
	    	
    	}
    	
    	return entropy;
    }
    
    



    public static void main(String[] args) throws IOException
    {
		new DTGenerator();
    }
	
	
	
	
}
