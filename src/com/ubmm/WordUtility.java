package com.ubmm;

import java.util.Random;

public class WordUtility {
	public static String[] wordList = { "HELP", "CUT", "DRAW", "CHICKEN", 
		"TRAIN", "WOMAN", "FUNNY", "BORING", "HAPPY", "SAD", "EXCITED" };
	
	public static char[] shuffleWord(char[] arr) {
		int l = arr.length;
		char t;
		int p;
		Random r = new Random();
		
		for (int i=l; i>1; i--) {
			// swap rand[0,i-1] and i-1
			p = r.nextInt(i); // p=0..i-1
			if (p==(i-1)) continue;
			t = arr[p];
			arr[p]=arr[i-1];
			arr[i-1]=t;
		}
		
		return arr;
	}
	
	public static char[] getRandomLetters(int letternum, String word) {
		char[] temp = new char[letternum];
		char[] wordarr=word.toUpperCase().toCharArray();
		int i;
		
		for (i=0; i<wordarr.length; i++) {
			if (i>=temp.length) return shuffleWord(temp);
			temp[i]=wordarr[i];
		}
		
		Random r =  new Random();
		for (;i<temp.length;i++)
			temp[i] = (char)(r.nextInt(25)+'A');
			
		return shuffleWord(temp);
	}
	
	public static String getRandomWord() {
		Random r = new Random();
		return wordList[r.nextInt(wordList.length)];
	}
	
	public static String[] getRandomThreeWords() {
		
		int l = wordList.length;
		String[] s = new String[3];
		int[] arr = new int[l];
		Random r = new Random();
		
		for (int i=0; i<l; i++) {
			arr[i]=i;
		}
		
		for (int i=0; i<3; i++) {
			int j=i+r.nextInt(l-i);
			int temp = arr[i];
			arr[i]=arr[j];
			arr[j]=temp;
			s[i] = wordList[arr[i]];
		}
		
		return s;
	}
	
}
