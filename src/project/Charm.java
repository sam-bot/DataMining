package project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Charm {
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
		Charm e = new Charm(_inFile, 0.01);
		System.out.println();
	}

	public Charm(String fileName, double minSupport) throws Exception {
		startTime = System.currentTimeMillis();
		transactions = readTransactionsFromFile(fileName);
		fileReadTime = System.currentTimeMillis();

		System.out.println("File Read in " + ((fileReadTime - startTime))
				+ " mil seconds for " + itemCount + " items\n");

		double supDouble = minSupport * transactions.size();
		min_sup = (int) supDouble;
		createVerticalFormat();
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
		constructCandidates();// min_sup=2
		counter = 0;
		System.out.println("Pass 2 discovered " + itemCount
				+ " frequent itemsets "
				+ (System.currentTimeMillis() - startTime) + " mil seconds");
		for (HashSet<Integer> i : tidSetCopy) {
			System.out.print(verticalCopy.get(counter) + " ");
			int count = 0;
			for (Integer k : i) {
				count++;
			}
			double size = transactions.size();
			Integer prefixIndex = verticalCopy.get(tidSetCopy.indexOf(i))
					.get(0);
			Integer prefixIndex2 = verticalCopy.get(tidSetCopy.indexOf(i)).get(
					1);
			Integer p2 = verticalItemset.indexOf(prefixIndex);
			Integer p3 = verticalItemset.indexOf(prefixIndex2);
			Integer prefix = tidSet.get(p2).size();
			Integer prefix2 = tidSet.get(p3).size();
			if (p3 < p2)
				prefix = prefix2;
			double support = prefix - count;
			double support2 = support / size;
			support2 = support2 * 100;
			System.out.println("Support = " + df.format(support2) + "% ("
					+ (int) support + " transactions)");
			counter++;
		}
		System.out.println();
		constructAdditionalCandidates();
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
				}
				counter++;
			}
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

	private void constructCandidates() {
		// copies created to maintain as master in k+1 candidate generation
		tidSetCopy = new ArrayList<HashSet<Integer>>(); // transaction IDs
		verticalCopy = new ArrayList<ArrayList<Integer>>();
		itemCount = 0;
		for (int i = 0; i < tidSet.size(); i++) {
			for (int k = i + 1; k < tidSet.size(); k++) {
				if (checkIntersect(tidSet.get(i), tidSet.get(k))
						&& (intersect(tidSet.get(i), tidSet.get(k)).size() >= min_sup)) {
					tidSetCopy.add(diffSet(tidSet.get(i), tidSet.get(k)));
					ArrayList<Integer> t1 = new ArrayList<Integer>();
					t1.add(verticalItemset.get(i));
					t1.add(verticalItemset.get(k));
					HashSet<Integer> hs = new HashSet<Integer>();
					hs.addAll(t1);
					t1.clear();
					t1.addAll(hs);
					verticalCopy.add(t1);
					itemCount++;
				}
			}
		}
	}

	// if one element in common, return true
	private Boolean checkIntersect(HashSet<Integer> t1, HashSet<Integer> t2) {
		for (int i : t2) {
			if (t1.contains(i))
				return true;
		}
		return false;
	}

	private void constructAdditionalCandidates() {
		// copies created to maintain as master in k+1 candidate generation
		ArrayList<Integer> t1 = new ArrayList<Integer>();
		ArrayList<HashSet<Integer>> tidTemp = new ArrayList<HashSet<Integer>>();
		ArrayList<ArrayList<Integer>> vertTemp = new ArrayList<ArrayList<Integer>>();
		DecimalFormat df = new DecimalFormat("####0.00");
		int pass = 2;
		int size = 3;
		do {
			pass++;
			moreSets = false;
			itemCount = 0;
			for (int i = 0; i < tidSetCopy.size(); i++) {
				for (int k = i + 1; k < tidSetCopy.size(); k++) {
					t1 = new ArrayList<Integer>();
					t1.addAll(verticalCopy.get(i));
					t1.addAll(verticalCopy.get(k));
					HashSet<Integer> hs = new HashSet<Integer>();
					hs.addAll(t1);
					t1.clear();
					t1.addAll(hs);
					if (hs.size() == size) {
						if (checkIntersect(tidSetCopy.get(i), tidSetCopy.get(k))
								&& (intersect(tidSetCopy.get(i),
										tidSetCopy.get(k)).size() >= min_sup)
								&& (!tidTemp.contains(intersect(
										tidSetCopy.get(i), tidSetCopy.get(k))))) {
							tidTemp.add(diffSet(tidSetCopy.get(i),
									tidSetCopy.get(k)));
							vertTemp.add(t1);
							itemCount++;
							moreSets = true;
						}
					}
				}
			}
			tidSetCopy.addAll(tidTemp);
			verticalCopy.addAll(vertTemp);
			int counter = 0;
			System.out
					.println("Pass " + pass + " discovered " + itemCount
							+ " frequent itemsets "
							+ (System.currentTimeMillis() - startTime)
							+ " mil seconds");
			for (HashSet<Integer> i : tidTemp) {
				System.out.print(vertTemp.get(counter) + " ");
				int count = 0;
				for (Integer k : i) {
					count++;
				}
				double size2 = transactions.size();
				Integer prefixIndex = verticalCopy.get(tidSetCopy.indexOf(i))
						.get(0);
				Integer prefixIndex2 = verticalCopy.get(tidSetCopy.indexOf(i))
						.get(1);
				Integer p2 = verticalItemset.indexOf(prefixIndex);
				Integer p3 = verticalItemset.indexOf(prefixIndex2);
				Integer prefix = tidSet.get(p2).size();
				Integer prefix2 = tidSet.get(p3).size();
				if (p3 < p2)
					prefix = prefix2;
				double support = prefix - count;
				double support2 = support / size2;
				support2 = support2 * 100;
				System.out.println("Support = " + df.format(support2) + "% ("
						+ (int) support + " transactions)");
				counter++;
			}
			System.out.println();
			tidTemp.clear();
			vertTemp.clear();
			size++;
		} while (moreSets == true);
	}

	private static HashSet<Integer> intersect(HashSet<Integer> f,
			HashSet<Integer> s) {
		HashSet<Integer> temp = new HashSet<Integer>(f);
		temp.retainAll(s);
		return temp;
	}

	private static HashSet<Integer> diffSet(HashSet<Integer> f,
			HashSet<Integer> s) {
		HashSet<Integer> temp = new HashSet<Integer>(s);
		temp.removeAll(f);
		return temp;
	}
}