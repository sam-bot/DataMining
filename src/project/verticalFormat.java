package project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class verticalFormat {
	private ArrayList<int[]> transactions; // All transactions from the file
	private List<int[]> _itemsets; // Current Working itemsets
	private List<Integer> tid; // transaction id's
	private ArrayList<Integer> verticalItemset;// 1-itemsets
	private ArrayList<ArrayList<Integer>> verticalCopy;// used to generate k+1
	private ArrayList<HashSet<Integer>> tidSet; // vertical transactions
	private ArrayList<HashSet<Integer>> tidSetCopy; // vertical transactions
	private final int _maxItemID = 999999;
	private static String _inFile;
	private int min_sup;
	private long startTime;
	private long fileReadTime;
	private int itemCount;
	Boolean moreSets = false;

	public static void main(String[] args) throws Exception {
		_inFile = args[0];
		verticalFormat e = new verticalFormat(_inFile, 0.001);
		writeFile();
		System.out.println();
	}

	public verticalFormat(String fileName, double minSupport) throws Exception {
		startTime = System.currentTimeMillis();
		transactions = readTransactionsFromFile(fileName);
		fileReadTime = System.currentTimeMillis();

		System.out.println("File Read in " + ((fileReadTime - startTime))
				+ " mil seconds for " + itemCount + " items\n");

		double supDouble = minSupport * transactions.size();
		min_sup = (int) supDouble;
		// int transactionSize = transactions.size();
		System.gc();
		createVerticalFormat();
		System.gc();
		tid.clear();
		_itemsets.clear();
		// transactions.clear();
		createCandidates();
		int counter = 0;
		System.out.println("Pass 1 discovered " + itemCount
				+ " frequent itemsets "
				+ (System.currentTimeMillis() - startTime) + " mil seconds");
		DecimalFormat df = new DecimalFormat("####0.00");
		for (HashSet<Integer> i : tidSet) {
			if (!i.isEmpty()) {
				System.out.print(verticalItemset.get(tidSet.indexOf(i)) + " ");
				double count = 0;
				for (Integer k : i) {
					count++;
				}
				double size = transactions.size();
				double support = count / size;
				support = support * 100;
				System.out.println("Support = " + df.format(support) + "% ("
						+ (int) count + " transactions)");
			}
		}
		System.out.println();
	}

	private static void writeFile() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("testFile.txt", "UTF-8");
		writer.println("The first line");
		writer.println("The second line");
		writer.close();
	}

	@SuppressWarnings("resource")
	ArrayList<int[]> readTransactionsFromFile(String fileName) {
		ArrayList<int[]> trans = new ArrayList<int[]>();
		_itemsets = new ArrayList<int[]>();
		tid = new ArrayList<Integer>();
		itemCount = 0;
		HashMap<Integer, Integer> uniqueItems = new HashMap<Integer, Integer>(); // temporary
		BufferedReader data_in;
		try {
			data_in = new BufferedReader(new FileReader(fileName));
			int tidCounter = 0;
			while (data_in.ready()) {
				String str = data_in.readLine();
				String[] strItems = str.split(",");
				int[] items_in_trans = new int[strItems.length];
				for (int i = 0; i < strItems.length; i++) {
					// Parse each items in each transaction into an array
					items_in_trans[i] = Integer.parseInt(strItems[i]);
					int[] item = new int[] { items_in_trans[i] };
					// preload all of the the unique items
					if (!uniqueItems.containsKey(items_in_trans[i])) {
						if (items_in_trans[i] >= _maxItemID) {
							throw new Exception("Max ItemID reached");
						}
						uniqueItems.put(items_in_trans[i], items_in_trans[i]);
						_itemsets.add(item);
					}
					itemCount++;
				}
				trans.add(items_in_trans);
				tid.add(tidCounter);
				tidCounter++;
			}
			data_in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trans;
	}

	private void createVerticalFormat() {
		verticalItemset = new ArrayList<Integer>(); // transactions
		tidSet = new ArrayList<HashSet<Integer>>(); // transaction IDs
		int counter;
		for (int[] i : transactions) {
			counter = 0;
			for (int k = 0; k < i.length; k++) {
				if (verticalItemset.contains(i[counter])) {
					tidSet.get(verticalItemset.indexOf(i[counter])).add(
							tid.get(transactions.indexOf(i)));
				} else {
					verticalItemset.add(i[counter]);
					tidSet.add(new HashSet<Integer>(tid.get(transactions
							.indexOf(i))));
					tidSet.get(verticalItemset.indexOf(i[counter])).add(
							tid.get(transactions.indexOf(i)));
				}
				counter++;
			}
			i = null;
			// tid(i)=null;
			System.gc();
			// java.lang.Runtime.getRuntime().gc();
		}
	}

	private void createCandidates() {
		itemCount = 0;
		for (int i = 0; i < tidSet.size(); i++) {
			if (tidSet.get(i).size() < min_sup) {
				tidSet.get(i).clear();
				verticalItemset.set(i, null);
			} else {
				itemCount++;
			}
		}

	}
}