package homework;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.Sets;

public class Apriori {
	static ArrayList<String> data = new ArrayList<String>();
	static ArrayList<String> dataCopy = new ArrayList<String>();
	static ArrayList<Character> candidate = new ArrayList<Character>();
	static ArrayList<Integer> candidateCount = new ArrayList<Integer>();

	public static void main(String[] args) throws Exception {
		dataCopy = readFile("dmdata.txt"); // copy for manipulation purposes
		firstPass();
		System.out.println("Frequent 1-itemsets: ");
		for (Character s : candidate) {
			System.out.print(s + " ");
			int index = candidate.indexOf(s);
			int count = candidateCount.get(index);
			System.out.println(count);
		}
		removeBelowMin();
		System.out.println("candidates with minimum support: ");
		for (Character s : candidate) {
			System.out.print(s + " ");
			int index = candidate.indexOf(s);
			int count2 = candidateCount.get(index);
			System.out.println(count2);
		}
		testCandidates();
	}

	// read transactions into arrayList
	public static ArrayList<String> readFile(String file)
			throws FileNotFoundException {
		File here = new File(file);
		Scanner scanner = new Scanner(here);
		while (scanner.hasNext()) {
			data.add(scanner.next());
		}
		scanner.close();
		// data is the master copy
		return data;
	}

	// generate frequent 1-itemsets
	private static void firstPass() {
		for (String s : data) {
			char[] copyFrom = s.toCharArray();
			for (Character t : copyFrom) {
				if (candidate.contains(t)) {
					checkChar(t);
				} else {
					candidate.add(t);
					candidateCount.add(0);
					checkChar(t);
				}
			}
		}
	}

	// count character appearances
	private static void checkChar(Character t) {
		for (String s : dataCopy) {
			char[] copyFrom = s.toCharArray();
			for (Character r : copyFrom) {
				if (r == t) {
					int index = candidate.indexOf(t);
					int count = candidateCount.get(index);
					candidateCount.set(index, count + 1);
					// remove char in string array
					String rs = Character.toString(r);
					if (dataCopy.contains(s)) {
						dataCopy.set(dataCopy.indexOf(s), s.replace(rs, ""));
					}
				}
			}
		}
	}

	// remove 1-itemsets below minimum support of 50% (27)
	private static void removeBelowMin() {
		ArrayList<Integer> copy = new ArrayList(candidateCount);
		int count = dataCopy.size() / 2;
		for (int s : copy) {
			if (s < count) {
				int index = candidateCount.indexOf(s);
				candidateCount.remove(index);
				candidate.remove(index);
			}
		}
	}

	// create all combinations of frequent 1-itemsets using Guava powerSet
	private static Set<Set<Character>> getCombos() {
		Set<Character> foo = new HashSet<Character>(candidate);
		Set<Integer> foo2 = new HashSet<Integer>(candidateCount);
		Set<Set<Character>> foo3 = Sets.powerSet(foo);
		for (Set<Character> subSet : foo3) {
			System.out.println(subSet);
		}
		return foo3;
	}

	private static void testCandidates() {
		Set<Set<Character>> foo4 = getCombos();
		for (Set<Character> s : foo4) {
			String temp = null;
			for (Character c : s) {
				temp += c;
			}
			for (String j : data) {
				// start here
			}
		}
	}
}