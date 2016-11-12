/*
@Author: Prashanth Seralathan
Date Edited: 10/16/2016
Place  : Buffalo, NY, USA.
Subject: Information Retrieval - Fall 2016
Program : Boolean Query Retrieval
Methodologies :
 - Term at a time
 - Document at a time
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class CSE535Project2 {
	
	Directory index;
	String file_loc = "E:/SUNY Buffalo - MS in CS/Course Materials/Information Retrieval/Project 2/index";
	//String file_loc = index_path;
	static HashMap<String, LinkedList<Integer>> term_list = new HashMap<String, LinkedList<Integer>>();	
	//static ArrayList<LinkedList<Integer>> postingsList = new ArrayList<LinkedList<Integer>>(100);
	//static ArrayList<LinkedList<Integer>> postingsListDAATAnd = new ArrayList<LinkedList<Integer>>(100);
	//static ArrayList<LinkedList<Integer>> postingsListDAATOr = new ArrayList<LinkedList<Integer>>(100);
	static LinkedList<ListIterator> postingsListIterator;
	static LinkedList<Integer> result_set;
	static int comparsions_count = 0;
	static int taat_OR_comparsions_count = 0;
	static int taat_AND_comparsions_count = 0;
	int count =0;
	private static BufferedWriter bufferFileWriter;
		
		public void initialRead(String file_loc){
			{	
			
			try
			{	
				FileSystem fs = FileSystems.getDefault();
				Path path = fs.getPath(file_loc);
				//Path path = Paths.get("E:/SUNY Buffalo - MS in CS/Course Materials/Information Retrieval/Project 2/index");
				index = FSDirectory.open(path);
				//System.out.println("Print Index" + index);
				this.performRead();
				
			}
			catch(IOException e){
				System.out.println("Error in Index path provided!");
				return;
			}
			
			}
		}
		
/*
 * Reads the Index file given in the path and creates an Inverted Index.
 * The Inverted index is of the following Format
 * <Terms> - [Postings List]
 * Example - <afskrivning,[5,13,21,25,31]>
 */
		public void performRead(){
			try {
				DirectoryReader rd = DirectoryReader.open(index);
				
				String termstore = " ";
				
				int doc_id = 0;
				Fields fields = MultiFields.getFields(rd);
				if (fields != null && fields.iterator().hasNext())
						{
							for(String field : fields)
							{
							if(field.startsWith("text_")){
								/*System.out.println(field);*/
								Terms terms = fields.terms(field);
								TermsEnum termsEnum = terms.iterator();
								BytesRef term_posting;
								PostingsEnum postings;
								while((term_posting = termsEnum.next()) != null){
									
									//termString = term_posting.utf8ToString();
									String termString = " ";
									termString = term_posting.utf8ToString();
									postings = MultiFields.getTermDocsEnum(rd, field , term_posting);
									while(postings.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
										//System.out.println("field=" + field + "; text=" + termString + " Postings: " + postings.docID());
										doc_id = postings.docID();
										
										if(!termString.equals(termstore)){
											term_list.put(termString, new LinkedList<Integer>());
											term_list.get(termString).add(doc_id);
											termstore = termString;		
										}
										else if(termString.equals(termstore)){
											term_list.get(termString).add(doc_id);	
											}
										}
											
									}
								}
							}			
						}
				
			}catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error in Creating the Index");
				e.printStackTrace();
				
			}
			
			/*
			for(String key: term_list.keySet()){
				//System.out.println("Hash Map values");
				System.out.println(" ");
				System.out.print("Term " + key.toString() + " Postings " + term_list.get(key));
				count = count + 1;
				
			}*/
			
			//System.out.println("Number of Terms " + count);	
			
		}
		
/*
 * This method is used to obtain the postings list for the query provided.
 * Input: String array of the Query Terms
 * Output: Postings List of the Query terms
 */
		
public static void getPostingList(String[] terms) throws IOException, Exception{
			Object posting;
			for(int i=0; i<terms.length; i++){
				//System.out.println("GetPostings");
				bufferFileWriter.write("GetPostings");
				bufferFileWriter.newLine();
				bufferFileWriter.write(terms[i]);
				//System.out.println(terms[i]);
				//System.out.print("Postings list: ");
				bufferFileWriter.newLine();
				bufferFileWriter.write("Postings list: ");
				if(term_list.containsKey(terms[i]))
				{
					Iterator it = term_list.get(terms[i]).iterator();
					while(it.hasNext()){
						if((posting = it.next()) != null){
							//System.out.print(posting.toString() + " ");
							bufferFileWriter.write(posting.toString() + " ");
						}
						
						
					}
					//System.out.println("");
					bufferFileWriter.newLine();
				}		
			}	
		}
	
/*
 * DAAT OR - Document at a time Traversal - Union Operation
 * All the postings lists of the query terms are traversed at once.
 * Strategy - Find minimum element of all the linked lists of the query terms and pop it out at once.
 * 			  If the same minimum element is found across multiple linked lists special handling is required.
 * 			  Speical Handling involves removal of the minimum elements across all the linked lists by maintaining a pointer in the ArrayList.
 */

public static void getPostingListDaatOR(String[] terms) throws IOException, Exception{
			
			//ArrayList<LinkedList<Integer>> postingsList = new ArrayList<LinkedList<Integer>>(1000);
			int docCount = 0;
			int min = Integer.MAX_VALUE;
			LinkedList<Integer> merged_result_DAATOR = new LinkedList();
			int comparsions_count_DAAT_OR = 0;
			int i =0;
			int pointer_1 = 0;
			int Matcher = 0;
			int list_to_be_removed;
			//int list_to_be_removed_spl = -1;
			boolean special_removal = false;
			boolean to_be_removed = false;
			ArrayList<Integer> list_to_be_removed_spl;
			int count = 0;
			for(String term: terms){
				++i;
			}
			
			ArrayList<LinkedList<Integer>> postingsList = new ArrayList<LinkedList<Integer>>(i);
			bufferFileWriter.write("DaatOr");
			bufferFileWriter.newLine();
			for(String term: terms){
				if(term_list.get(term)!= null){
					docCount = docCount + term_list.get(term).size();
					postingsList.add((LinkedList<Integer>)term_list.get(term).clone());
					bufferFileWriter.write(term + " ");
					//System.out.println("Postings List " + postingsList.get(i));
					//System.out.println("Postings List :" + postingsList.toString());
					//System.out.println("Posting List Size : " + postingsList.size());
				}
			}
			
			//System.out.println("Before the merge" + postingsList.toString() );
			
			Matcher = 1;
			list_to_be_removed = -1;
			list_to_be_removed_spl = new ArrayList(postingsList.size());
			//int list_removal_pointer = 0;
			
			while(pointer_1 < postingsList.size() && postingsList.get(pointer_1).peek() != null){	
					//System.out.println("Peek at Outer Pointers " + postingsList.get(pointer_1));
					Matcher = pointer_1 + 1;
					
					if(min > postingsList.get(pointer_1).peek())
					{   ++comparsions_count_DAAT_OR;
						min = postingsList.get(pointer_1).peek();
						list_to_be_removed = pointer_1;
						to_be_removed = true;
					}
					
		
					while(Matcher < postingsList.size()){
						//System.out.println("Minimum Value " + min);
						//System.out.println("Peek at Inner Pointers " + postingsList.get(Matcher).peek() + "Pointer value " + Matcher);
						if(( postingsList.get(Matcher).peek() != null && postingsList.get(Matcher).peek() == postingsList.get(pointer_1).peek()) || (postingsList.get(Matcher).peek() != null && postingsList.get(Matcher).peek() == min)){
							++comparsions_count_DAAT_OR;
							special_removal = true;
							list_to_be_removed_spl.add(Matcher);
							//System.out.println("List Added to Matcher  " +  Matcher);
							//list_removal_pointer++;
						}
						/*if(!(postingsList.get(Matcher).isEmpty()) && postingsList.get(Matcher).peek() != null && min == postingsList.get(Matcher).peek()){
							++comparsions_count_DAAT_OR;
							list_to_be_removed_spl = Matcher;
							System.out.println("Special Removal Array " +list_to_be_removed_spl);
							to_be_removed = true;
						}*/
						if(postingsList.get(Matcher).peek() != null && min > postingsList.get(Matcher).peek()){
							++comparsions_count_DAAT_OR;
							//System.out.println("Happens at the Value " + postingsList.get(Matcher).peek());
							min = postingsList.get(Matcher).peek();
							list_to_be_removed = Matcher;
							to_be_removed = true;
							
							/*Condition to handle - If minimum is found less than the stored value in special removal Array 
							 * Clear the special removal flag
							 * Clear speical removal list - So that new elements can be added in case if there's another hit with the new minumum
							*/
							
							if(special_removal == true){
								special_removal = false;
								list_to_be_removed_spl.clear();
							}
							//System.out.println("List to be removed updated in inner Loop " + list_to_be_removed);
						}
						Matcher++;
					}
					
					
					pointer_1 = 0;
					Matcher = 1;
					
					
						if(special_removal == true && list_to_be_removed_spl.size() != 0){
							merged_result_DAATOR.add(postingsList.get(list_to_be_removed).peek());
							for(int m=0; m < list_to_be_removed_spl.size(); m++){
								if(list_to_be_removed != list_to_be_removed_spl.get(m)){
									//merged_result_DAATOR.add(postingsList.get(list_to_be_removed).peek());
									//System.out.println("Peek at the element to be removed 1 " + postingsList.get(list_to_be_removed).peek());
									//int polled_element = postingsList.get(list_to_be_removed).poll();
									int polled_element = postingsList.get(list_to_be_removed_spl.get(m)).poll();	
								}
										
							}
							list_to_be_removed_spl.clear();
							
						}
						
						
						if(to_be_removed == true && special_removal == true) {
							//merged_result_DAATOR.add(postingsList.get(list_to_be_removed).peek());
							//System.out.println("Peek at the element to be removed 2 " + postingsList.get(list_to_be_removed).peek());
							int polled_element = postingsList.get(list_to_be_removed).poll();
						}
						else if(to_be_removed == true && special_removal == false){
							merged_result_DAATOR.add(postingsList.get(list_to_be_removed).peek());
							//System.out.println("Peek at the element to be removed 3 " + postingsList.get(list_to_be_removed).peek());
							int polled_element = postingsList.get(list_to_be_removed).poll();
							
						}
						
						
							
						//System.out.println("Merged List " + merged_result_DAATOR.toString());
						//System.out.println("After Removal of polled Element " + postingsList.get(list_to_be_removed).peek());
						min = Integer.MAX_VALUE;
						special_removal = false;
						//list_to_be_removed_spl = -1;
						to_be_removed = false;
						//list_to_be_removed_spl = -1;
						//System.out.println("Polled Element " + polled_element);
					
					while(pointer_1 < postingsList.size() && postingsList.get(pointer_1).peek() == null){
						pointer_1++;	
					}
					while(Matcher < postingsList.size() && postingsList.get(Matcher).peek() == null){
						Matcher++;
					}
					//System.out.println("Polled Element " + polled_element);
			}
			
			
			bufferFileWriter.newLine();
			bufferFileWriter.write("Results: ");
			for(int j=0; j < merged_result_DAATOR.size(); j++){
				bufferFileWriter.write(merged_result_DAATOR.get(j) + " ");		
			}
			
			bufferFileWriter.newLine();
			bufferFileWriter.write("Number of documents in results: " + merged_result_DAATOR.size());
			bufferFileWriter.newLine();
			bufferFileWriter.write("Number of comparisons: " + comparsions_count_DAAT_OR);
			bufferFileWriter.newLine();
			
			
			//System.out.println("DAATOR after the merge :" + merged_result_DAATOR.toString());
			//System.out.println("Number of Comparasions in DAATOR " + comparsions_count_DAAT_OR);
			//System.out.println("Document Size " + merged_result_DAATOR.size());
			
			
		}
		

/*
* DAAT AND - Document at a time Traversal - Intersection Operation
* All the postings lists of the query terms are traversed at once.
* Strategy -  Find minimum element of all the linked lists of the query terms and pop it out at once.
 * 			  If the same minimum element is found across multiple linked lists special handling is required.
 * 			  Speical Handling involves removal of the minimum elements across all the linked lists by maintaining a pointer in the ArrayList.
 * 	While handling the special case of same minimum element across all the Linkedlists - Add it to the Final Merged Linked List.
 * final merged list.
*/


public static void	getPostingListDaatAND(String[] terms) throws IOException, Exception{
	int docCount = 0;
	int min = Integer.MAX_VALUE;
	LinkedList<Integer> merged_result_DAATAND = new LinkedList();
	int comparsions_count_DAAT_AND = 0;
	int i =0;
	int pointer_1 = 0;
	int Matcher = 0;
	int list_to_be_removed;
	//int list_to_be_removed_spl = -1;
	boolean special_removal = false;
	boolean to_be_removed = false;
	int Matching_count = 0;
	ArrayList<Integer> list_to_be_removed_spl;
	int count = 0;
	for(String term: terms){
		++i;
	}
	
	ArrayList<LinkedList<Integer>> postingsListDaatAnd = new ArrayList<LinkedList<Integer>>(i);
	bufferFileWriter.write("DaatAnd");
	bufferFileWriter.newLine();
	for(String term: terms){
		if(term_list.get(term)!= null){
			docCount = docCount + term_list.get(term).size();
			postingsListDaatAnd.add((LinkedList<Integer>)term_list.get(term).clone());
			bufferFileWriter.write(term + " ");
			//System.out.println("Postings List " + postingsListDaatAnd.get(i));
			//System.out.println("Postings List :" + postingsListDaatAnd.toString());
			//System.out.println("Posting List Size : " + postingsListDaatAnd.size());
		}
	}
	
	//System.out.println("Before the merge" + postingsListDaatAnd.toString() );
	
	Matcher = 1;
	list_to_be_removed = -1;
	list_to_be_removed_spl = new ArrayList(postingsListDaatAnd.size());
	//int list_removal_pointer = 0;
	
	if(i == 1){
		//If only one term is given merge it onto the final list
		for(int merged_counter = 0; merged_counter < term_list.get(terms[0]).size() ; merged_counter++){
			merged_result_DAATAND.add(term_list.get(terms[0]).get(merged_counter));
		}
	}
	
	else{	
	
	while(pointer_1 < postingsListDaatAnd.size() && postingsListDaatAnd.get(pointer_1).peek() != null){	
			//System.out.println("Peek at Outer Pointers " + postingsListDaatAnd.get(pointer_1));
			Matcher = pointer_1 + 1;
			
			if(min > postingsListDaatAnd.get(pointer_1).peek())
			{   ++comparsions_count_DAAT_AND;
				min = postingsListDaatAnd.get(pointer_1).peek();
				list_to_be_removed = pointer_1;
				to_be_removed = true;
			}
			

			while(Matcher < postingsListDaatAnd.size()){
				//System.out.println("minimum Value " + min);
				//System.out.println("Peek at Inner Pointers " + postingsListDaatAnd.get(Matcher).peek() + "Pointer value " + Matcher);
				if(( postingsListDaatAnd.get(Matcher).peek() != null && postingsListDaatAnd.get(Matcher).peek() == postingsListDaatAnd.get(pointer_1).peek()) || (postingsListDaatAnd.get(Matcher).peek() != null && postingsListDaatAnd.get(Matcher).peek() == min)){
					++comparsions_count_DAAT_AND;
					special_removal = true;
					list_to_be_removed_spl.add(Matcher);
					Matching_count++;
					//System.out.println("List Added to Matcher  " +  Matcher);
					//list_removal_pointer++;
				}
				
				
				if(postingsListDaatAnd.get(Matcher).peek() != null && min > postingsListDaatAnd.get(Matcher).peek()){
					++comparsions_count_DAAT_AND;
					//System.out.println("Happens at the Value " + postingsListDaatAnd.get(Matcher).peek());
					min = postingsListDaatAnd.get(Matcher).peek();
					list_to_be_removed = Matcher;
					to_be_removed = true;
					
					/*Condition to handle - If minimum is found less than the stored value in special removal Array 
					 * Clear the special removal flag
					 * Clear speical removal list - So that new elements can be added in case if there's another hit with the new minumum
					*/
					
					if(special_removal == true){
						special_removal = false;
						list_to_be_removed_spl.clear();
					}
					//System.out.println("List to be removed updated in inner Loop " + list_to_be_removed);
				}
				Matcher++;
			}
			
			
			pointer_1 = 0;
			Matcher = 1;
			
			
				if(special_removal == true && list_to_be_removed_spl.size() != 0){
					if(Matching_count == postingsListDaatAnd.size() -1)
					{
						merged_result_DAATAND.add(postingsListDaatAnd.get(list_to_be_removed).peek());
						for(int m=0; m < list_to_be_removed_spl.size(); m++){
							if(list_to_be_removed != list_to_be_removed_spl.get(m)){
								//merged_result_DAATAND.add(postingsListDaatAnd.get(list_to_be_removed).peek());
								//System.out.println("Peek at the element to be removed 1 " + postingsListDaatAnd.get(list_to_be_removed).peek());
								//int polled_element = postingsListDaatAnd.get(list_to_be_removed).poll();
								int polled_element = postingsListDaatAnd.get(list_to_be_removed_spl.get(m)).poll();	
						}
								
					}
					}
					else{
						for(int m=0; m < list_to_be_removed_spl.size(); m++){
							if(list_to_be_removed != list_to_be_removed_spl.get(m)){
								//merged_result_DAATAND.add(postingsListDaatAnd.get(list_to_be_removed).peek());
								//System.out.println("Peek at the element to be removed 1 " + postingsListDaatAnd.get(list_to_be_removed).peek());
								//int polled_element = postingsListDaatAnd.get(list_to_be_removed).poll();
								int polled_element = postingsListDaatAnd.get(list_to_be_removed_spl.get(m)).poll();	
							}
						
					}
						
				}
					list_to_be_removed_spl.clear();
				}
				
				if(to_be_removed == true && special_removal == true) {
					//merged_result_DAATAND.add(postingsListDaatAnd.get(list_to_be_removed).peek());
					//System.out.println("Peek at the element to be removed 2 " + postingsListDaatAnd.get(list_to_be_removed).peek());
					int polled_element = postingsListDaatAnd.get(list_to_be_removed).poll();
				}
				else if(to_be_removed == true && special_removal == false){
					//merged_result_DAATAND.add(postingsListDaatAnd.get(list_to_be_removed).peek());
					//System.out.println("Peek at the element to be removed 3 " + postingsListDaatAnd.get(list_to_be_removed).peek());
					int polled_element = postingsListDaatAnd.get(list_to_be_removed).poll();
					
				}	
				//System.out.println("Merged List " + merged_result_DAATAND.toString());
				//System.out.println("After Removal of polled Element " + postingsListDaatAnd.get(list_to_be_removed).peek());
				min = Integer.MAX_VALUE;
				Matching_count = 0;
				special_removal = false;
				//list_to_be_removed_spl = -1;
				to_be_removed = false;
				//list_to_be_removed_spl = -1;
				//System.out.println("Polled Element " + polled_element);
			
			while(pointer_1 < postingsListDaatAnd.size() && postingsListDaatAnd.get(pointer_1).peek() == null){
				pointer_1++;	
			}
			while(Matcher < postingsListDaatAnd.size() && postingsListDaatAnd.get(Matcher).peek() == null){
				Matcher++;
			}
			//System.out.println("Polled Element " + polled_element);
	}
	}
	
	
	bufferFileWriter.newLine();
	bufferFileWriter.write("Results: ");
	if(merged_result_DAATAND.size() > 0){
		for(int j=0; j < merged_result_DAATAND.size(); j++){
			bufferFileWriter.write(merged_result_DAATAND.get(j) + " ");		
		}
	}
	else{
		bufferFileWriter.write("empty");
	}
	
	
	bufferFileWriter.newLine();
	bufferFileWriter.write("Number of documents in results: " + merged_result_DAATAND.size());
	bufferFileWriter.newLine();
	bufferFileWriter.write("Number of comparisons: " + comparsions_count_DAAT_AND);
	bufferFileWriter.newLine();
	
	
	//System.out.println("DAATAND after the merge :" + merged_result_DAATAND.toString());
	//System.out.println("Number of Comparasions in DAATAND " + comparsions_count_DAAT_AND);
	//System.out.println("Document Size " + merged_result_DAATAND.size());
	
}

/*
 * TAAT - Term at a time - (Intersection Operation)
 * The Linkedlist are traversed on a term to term basis.
 * Input: String Array of the Query terms
 * Output: DocID's that contain all the query terms in them.
 * Strategy:
 * 	1) Take two linkedlists once, Compare and add it to intermediate list if two elements are found equal across the linked lists compared.
 * 	2) Use the intermediate Linkedlist to compare the next occurrence of the linked list and merge it further.
 * This ensures that documents are considered term at a time for comparison and merging.
 */
public static void getPostingsListTaatAND(String[] terms) throws IOException, Exception{
	int docCount = 0;
	taat_AND_comparsions_count = 0;
	int Taat_count = 0;
	//bufferFileWriter.newLine();
	bufferFileWriter.write("TaatAnd");
	bufferFileWriter.newLine();
	//System.out.println(" ");
	String terms_in_search = " ";
	for(String term: terms){
		if(term_list.get(term)!= null){
			//terms_in_search.concat(term + " ");
			bufferFileWriter.write(term + " ");
			docCount = docCount + term_list.get(term).size();
			//System.out.println("Document Size" + docCount);
			Taat_count = Taat_count + 1;
			//System.out.println("Taat count" + Taat_count);
		}
	}
	
	LinkedList<Integer> li1 = new LinkedList();
	LinkedList<Integer> li2 = new LinkedList();
	
	
	if(Taat_count == 1){
		//If only one term is given merge it onto the final list
		li1 = (LinkedList<Integer>) term_list.get(terms[0]).clone();
	}
	else{
		int i=0;
		while(i < Taat_count - 1){
				if(i == 0){
					li1 = (LinkedList<Integer>) term_list.get(terms[i]).clone();
						  }
				li2 = (LinkedList<Integer>) term_list.get(terms[i+1]).clone();
				//System.out.println("Lists" + li1 + li2);
				li1 = IntersectList(li1, li2);
				//System.out.println("List values after merge" + li1);
				i = i + 1;		
		}
		
	}
	
	
	//String concat_result = " ";
	bufferFileWriter.newLine();
	bufferFileWriter.write("Results: ");
	if(li1.size() > 0){
		for(int j=0; j < li1.size(); j++){
			bufferFileWriter.write(li1.get(j) + " ");		
		}
	}
	else{
		bufferFileWriter.write("empty");
	}
	
	bufferFileWriter.newLine();
	bufferFileWriter.write("Number of documents in results: " + li1.size());
	bufferFileWriter.newLine();
	bufferFileWriter.write("Number of comparisons: " + taat_AND_comparsions_count);
	
	/*
	System.out.println("Final values after merge" + li1);
	System.out.println("Comparasion Count of TAATAND " + taat_AND_comparsions_count);
	*/
	bufferFileWriter.newLine();
	
}
	
/*
 * TAAT - Term at a time - (Union Operation)
 * The Linkedlist are traversed on a term to term basis.
 * Input: String Array of the Query terms
 * Output: DOCid's of all the Query terms merged together
 * Strategy:
 * 	1) Take two linkedlists once, Compare and merge them based on the ascending order.
 * 	2) Use the merged Linkedlist to compare the next occurence of the linked list and merge it further.
 * This ensures that documents are considered term at a time for comparison and merging.
 */
public static void getPostingsListTaatOR(String[] terms) throws IOException, Exception{
			
			int docCount = 0;
			taat_OR_comparsions_count = 0;
			//bufferFileWriter.newLine();
			bufferFileWriter.write("TaatOr");
			bufferFileWriter.newLine();
			int Taat_count = 0;
			for(String term: terms){
				if(term_list.get(term)!= null){
					docCount = docCount + term_list.get(term).size();
					bufferFileWriter.write(term + " ");
					//System.out.println("Document Size" + docCount);
					Taat_count = Taat_count + 1;
					//System.out.println("Taat count" + Taat_count);
				}
			}
				
				LinkedList<Integer> li1 = new LinkedList();
				LinkedList<Integer> li2 = new LinkedList();
				
				if(Taat_count > 1){
					int i=0;
					while(i < Taat_count - 1){
							if(i == 0){
								li1 = (LinkedList<Integer>) term_list.get(terms[i]).clone();
							}
							li2 = (LinkedList<Integer>) term_list.get(terms[i+1]).clone();
							//System.out.println("Lists" + li1 + li2);
							li1 = UnionList(li1, li2);
							//System.out.println("List values after merge" + li1);
							i = i + 1;
					}
					
				}
				
				else {
					int i = 0;
					li1 = ((LinkedList<Integer>) term_list.get(terms[i]).clone());
				}
			
				
				bufferFileWriter.newLine();
				bufferFileWriter.write("Results: ");
				for(int j=0; j < li1.size(); j++){
					bufferFileWriter.write(li1.get(j) + " ");		
				}
				
				bufferFileWriter.newLine();
				bufferFileWriter.write("Number of documents in results: " + li1.size());
				bufferFileWriter.newLine();
				bufferFileWriter.write("Number of comparisons: " + taat_OR_comparsions_count);
				bufferFileWriter.newLine();
				
				/*
				System.out.println("List values after merge" + li1);
				System.out.println("Comparasion Count of TAATOR " + taat_OR_comparsions_count);
				*/
		}

/* Intersection of Two Lists	
 * 	Performs the Intersection of two lists - TAAT
 * 	Here every element of the given loop is checked against all the elements of the next list, If something is found Equal - Add it into the merged list.
 * 	This merged List is passed on to the calling function and it's retained for further comparison. 
 */

			
public static LinkedList<Integer> IntersectList(LinkedList<Integer> l1, LinkedList<Integer> l2){
	
	LinkedList<Integer> intersect_list = new LinkedList();
	LinkedList<Integer> second_list = new LinkedList<Integer>();
	Iterator<Integer> l1Iterator;
	int doc_id = 0;
	
	if(l1.size() < l2.size()){
		l1Iterator = l1.listIterator();
		second_list = l2;
	}
	else{
		l1Iterator = l2.listIterator();
		second_list = l1;
	}
	
	
	//TAATintersectOuter:
	
	while(l1Iterator.hasNext()){
		doc_id = l1Iterator.next();
		
	TAATintersect:
		for(int m=0; m<second_list.size(); m++){
			
			if(second_list.get(m) > doc_id){
				++taat_AND_comparsions_count;
				break TAATintersect;
			}
			else if(doc_id == second_list.get(m)){
				++taat_AND_comparsions_count;
				intersect_list.add(doc_id);
				second_list.remove(m);
				break TAATintersect;
				}
			}	
		}	
	return intersect_list;
}

/*Performs Union of the two given LinkedList and returns the merged LinkedList to the calling Method
 * The Key here is merging the Linked list in ascending Order
 * Check if val(i) < val(j)
 * 	Add val(i) and increment i
 * Check if val(j) < val(i)
 * 	Add val(j) and increment j
 * If equal - Add one of them and increment both i and j.
 * If the list is iterated and all the elements are traversed, Add remaining elements of the list to the merged list.
*/

public static LinkedList<Integer> UnionList(LinkedList<Integer> l1, LinkedList<Integer> l2){
			
			LinkedList<Integer> merged_list = new LinkedList();
			
			int i=0;
			int j=0;
			
			while(i < l1.size() && j < l2.size() ){
				++taat_OR_comparsions_count;
				if(l1.get(i) < l2.get(j)){
					//++taat_OR_comparsions_count;
					merged_list.add(l1.get(i));
					i = i+1;	
				}
				else if(l1.get(i) > l2.get(j)){
					//++taat_OR_comparsions_count;
					merged_list.add(l2.get(j));
					j = j+1;
				}
				/*
				else if(l1.get(i) == l2.get(j)){
					++taat_OR_comparsions_count;
					merged_list.add(l1.get(i));
					i++;
					j++;
				}*/
				else{
					merged_list.add(l1.get(i));
					i++;
					j++;
				}
			}
			
			if(j == l2.size() && i < l1.size()){
				while(i < l1.size()){
					merged_list.add(l1.get(i));
					i++;	
				}
			}
			
			if(i == l1.size() && j < l2.size()){
				while(j < l2.size()){
					merged_list.add(l2.get(j));
					j++;	
				}
			}
			
			return merged_list;
		}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, Exception {
		// TODO Auto-generated method stub
		
		if(args.length != 3){
			 System.out.println("Arguments must be <Index> <Output> <Input>");
			 return;
		}
		
		
		String index_path = args[0];
		String outputFile = args[1];
		String inputFile = args[2];
		
		
		/*
		String index_path = "E:/SUNY Buffalo - MS in CS/Course Materials/Information Retrieval/Project 2/index";
		String inputFile = "input.txt";
		String outputFile = "output.txt";
		*/		
		
		CSE535Project2 i1 = new CSE535Project2();
		i1.initialRead(index_path);
		//i1.initialRead(file_loc);
		ArrayList<String[]> inputTerms = new ArrayList<String[]>();
		
		
		File output = new File(outputFile);
		//String inputFile = "input.txt";
		output.createNewFile();
		BufferedReader bufferFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"utf-8"));
		try{
			String lines;
			while ((lines = bufferFileReader.readLine()) != null) {
				String[] terms = lines.split(" ");
				inputTerms.add(terms);
				//System.out.println(line);
				}
		}
		finally{
				bufferFileReader.close();
				}
		
		try{
			FileWriter outputFileWriter = new FileWriter(output);
			bufferFileWriter = new BufferedWriter(outputFileWriter);
			Iterator it = inputTerms.iterator();
			while(it.hasNext()){
				String[] terms_input = (String[]) it.next();
				//System.out.println("Terms " + terms_input);
				CSE535Project2.getPostingList(terms_input);
				
				CSE535Project2.getPostingsListTaatAND(terms_input);
				
				CSE535Project2.getPostingsListTaatOR(terms_input);
				
				CSE535Project2.getPostingListDaatAND(terms_input);
				
				CSE535Project2.getPostingListDaatOR(terms_input);
				
							
			}
				
		}
		
		
		
		finally {
			bufferFileWriter.close();
		}
			
	}

}
