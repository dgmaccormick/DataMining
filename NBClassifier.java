/*
 * Program that uses a Naive Bayesian Classification algorithm to classify
 * data using prior probabilities.
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
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.lang.Math;

public class NBClassifier 
{
	private ArrayList<String> attributeNames; // list of attributes
	private Set<String> vocabulary;
	
	private int attributeIndex;
	private int numAttributes; // the number of attributes in the file
	private ArrayList<String[]> itemSet; // list to hold all of the tuples
	private ArrayList<String> C;
	private ArrayList<HashMap<String, Double>> probs;
	private ArrayList<Double> probsC;
	
	public NBClassifier() throws IOException
	{
		Scanner input = new Scanner(System.in);
		
		
		readTargetFile();
		System.out.print("Choose a testing file: ");
    	String testFileName = input.next();
    	
		System.out.println("Choose an attribute (by number):");
		for(int i = 1; i <= attributeNames.size(); i++)
		{
			System.out.println("    " + i + ". " + attributeNames.get(i-1));
		}
		int choice = input.nextInt() - 1;
		attributeIndex = choice;
		classify(choice);
		
		output(testFileName);
		
		System.out.println("Your results are stored in Rules.txt");
	}
	
	private void classify(int attribute)
	{
		// list of Hashmaps to hold P(w|c) values
		probs = new ArrayList<HashMap<String, Double>>();
				
		// get the target classes
		C = new ArrayList<String>();
		
		for(String[] arr : itemSet)
		{
			if(!C.contains(arr[attribute]))
			{
				C.add(arr[attribute]);
			}
		}
		
		probsC = new ArrayList<Double>();
		
		for(String c : C)
		{
			HashMap<String, Double> map = new HashMap<String, Double>();
			
			
			ArrayList<String[]>docs = new ArrayList<String[]>();
			
			// list of members of docs, maps to frequency
			HashMap<String, Integer> Text = new HashMap<String, Integer>();
			
			// number of word positions in Text
			int n = 0;
			
			for(String[]arr : itemSet)
			{
				if(arr[attribute].equals(c))
				{
					docs.add(arr);
					
					for(int i = 0; i < arr.length; i++)
					{
						if(i != attribute)
						{
							if(Text.containsKey(arr[i]))
							{
								Text.put(arr[i], Text.get(arr[i]) + 1);
							}
							else
							{
								Text.put(arr[i], 1);
							}
							n++;
						}
					}	
				}	
			}
			
			// calculate P(c)
			double probC = (double)docs.size() / (double)itemSet.size();			
			probsC.add(probC);
			
			
			// for each word in the vocabulary
			for(String w : vocabulary)
			{
				// frequency of word
				int freq = 0;
				if(Text.containsKey(w))
				{
					freq = Text.get(w);
				}
				
				
				// calculate P(w | c)
				double probWGivenC = (double)(freq + 1) / (double)(n + vocabulary.size());
				
				map.put(w, probWGivenC);
				
				//System.out.println("P("+ w + "|"+c+")="+probWGivenC);
			}
			probs.add(map);
			
		}	
	}
	
	/*
	 * method to read the test file and output results to the Results file
	 */
	public void output(String testFile) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter("Rules.txt"));
		
		
		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String currentLine = br.readLine();
		
		StringTokenizer st = new StringTokenizer(currentLine);
		
		numAttributes = st.countTokens();
		
		int countAccuracy = 0;
		int numLines = 0;
		
		ArrayList<String> testAttribs = new ArrayList<String>();
		
		attributeNames = new ArrayList<String>();
		for(int i = 0; i < numAttributes; i++)
		{
						
			String s = st.nextToken() + " ";
			
			if(i == attributeIndex)
				bw.write("class  ");
			else
				bw.write(s + "  ");
		}
		bw.write("Classification");
		bw.newLine();
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
    				bw.write(curr + "  ");

    			}
    		}
    		double[]dArr = new double[C.size()];
    		double dMax = 0;
    		String maxClass = "";
    		for(int i = 0; i < C.size(); i++)
    		{
    			double p = probsC.get(i); // P(c)
    			
    			double d = 1.0;
	    		for(int k = 0; k < numAttributes; k++)
	    		{
	    			if(k != attributeIndex)
	    				d = d * probs.get(i).get(items[k]);

	    		}
	    		
	    		d = d * p;
	    		dArr[i] = d;
	    		if(d > dMax)
	    		{
	    			dMax = d;
	    			maxClass = C.get(i);
	    		}
    		}
    		
    		if(maxClass.equals(items[attributeIndex]))
    			countAccuracy++;
    		
    		bw.write(maxClass);
    		bw.newLine();
    		numLines++;
		}
		
		bw.write("Accuracy: " + countAccuracy + "/" + numLines);
		bw.newLine();
		br.close();
		bw.close();
	}
	
		
	
	/*
     * method to store contents of database into
     */
    private void readTargetFile() throws IOException
    {
    	vocabulary = new HashSet<String>(); // distinct words in the data set
    	
    	System.out.print("Choose a training file: ");
    	Scanner input = new Scanner(System.in);
    	String filename1 = input.next();
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename1));
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
	        			vocabulary.add(curr);
        			}
        		}
        		
    			// make sure there is not already an equivalent item in the set
        		if(!itemSet.contains(items))
        			itemSet.add(items); 
        		else
        			System.out.println("Noise");
    		} 	
    		br.close();
    		//System.out.println("Set: " + vocabulary.toString());
    } // end of readFile method
	
	public static void main(String[]args) throws IOException
	{		
		new NBClassifier();	
	}
}
