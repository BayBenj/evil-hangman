package hangman;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Pattern;

import hangman.IEvilHangmanGame.GuessAlreadyMadeException;

public class EvilHangmanGame implements IEvilHangmanGame {
	
	private int _wordLength;
	private Stack s = new Stack();
	private StringBuilder sb = new StringBuilder();
	private int _guessesLeft = -1;
	private Set<String> _AvailableWords = new HashSet<String>();
	private String _VisibleWord = "";
	private SortedSet<String> _alreadyUsed = new TreeSet<String>();
	
	public void setGuesses(int guesses) {
		_guessesLeft = guesses;
	}
	
	public void startGame(File dictionary, int wordLength) throws FileNotFoundException {
		_wordLength = wordLength;
		String temp = "";
		for (int i = 0; i < wordLength; i++) {
			temp += '_';
		}
		_VisibleWord = temp;
		Scanner dic = new Scanner(dictionary);
		while (dic.hasNext()) {
			String word = dic.next();
			if (word.length() == wordLength) {
				_AvailableWords.add(word);
			}
		}
		dic.close();		
	}
	
	public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
		_alreadyUsed.add(Character.toString(guess));
		Map<String,Set<String>> map = new HashMap<String,Set<String>>();
		for (String s : _AvailableWords) {
			String patt = makePattern(s, guess);
			if (map.containsKey(patt)) {
				Set<String> temp = map.get(patt);
				temp.add(s);
				map.put(patt, temp);
			}
			else {
				Set<String> temp = new HashSet<String>();
				temp.add(s);
				map.put(patt, temp);
			}
		}
		_AvailableWords = findLargestSet(map, guess);
		return _AvailableWords;
	}
	
	public Set<String> findLargestSet(Map<String,Set<String>> map, char guess) {
		Set<String> largest_set = new HashSet<String>();
		int fewest_new_chars = 100000;
		String pat = "";
		for (int i = 0; i < _wordLength; i++) {
			pat += '_';
		}
		String current_winning_pattern = pat;
		for (Map.Entry<String,Set<String>> entry : map.entrySet()) {
		    String pattern = entry.getKey();
		    Set<String> words = entry.getValue();
		    if (words.size() > largest_set.size()) {
		    	largest_set = words;
		    	pat = addPatterns(_VisibleWord, pattern);
		    	fewest_new_chars = getCharNum(pattern, guess);
		    	current_winning_pattern = pattern;
		    }
		    else if (words.size() == largest_set.size()) {
		    	if (!pattern.contains(Character.toString(guess))) {
		    		largest_set = words;
		    		pat = addPatterns(_VisibleWord, pattern);
		    		fewest_new_chars = getCharNum(pattern, guess);
		    		current_winning_pattern = pattern;
		    	}
		    	else {
		    		int count = getCharNum(pattern, guess);
		    		if (count < fewest_new_chars) {
		    			fewest_new_chars = count;
		    			largest_set = words;
		    			pat = addPatterns(_VisibleWord, pattern);
		    			current_winning_pattern = pattern;
		    		}
		    		else if (count == fewest_new_chars) {
		    			for (int i = pattern.length() - 1; i >= 0; i--) {
		    				if (current_winning_pattern.charAt(i) != pattern.charAt(i)) {
		    					if (pattern.charAt(i) == guess) {
		    						largest_set = words;
					    			pat = addPatterns(_VisibleWord, pattern);
					    			current_winning_pattern = pattern;
					    			break;
		    					}
		    					else if (current_winning_pattern.charAt(i) == guess) {
		    						break;
		    					}
		    				}
		    			}
		    		}
		    	}
		    }
		}
		decideGuessOutcome(pat, guess);
		return largest_set;
	}
	
	public String addPatterns(String oldPattern, String newPattern) {
		StringBuilder sb = new StringBuilder(oldPattern);
		for (int i = 0; i < newPattern.length(); i++) {
			if (oldPattern.charAt(i) == '_' && oldPattern.charAt(i) != newPattern.charAt(i)) {
				sb.setCharAt(i, newPattern.charAt(i));
			}
		}
		return sb.toString();
	}
	
	public String makePattern(String word, char c) {
		StringBuilder s = new StringBuilder(word);
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != c) {
				s.setCharAt (i, '_');
			}
		}
		String str = s.toString();
		return str;
	}
	
	public int getCharNum(String pattern, char guess) {
		int count = 0;
		for (int i = 0; i < pattern.length(); i++) {
			 if (pattern.charAt(i) == guess) {
				 count++;
			 }
		}
		return count;
	}
	
	public void decideGuessOutcome(String pat, char guess) {
		if (_guessesLeft != -1) {
			if (_VisibleWord.equals(pat)) {
				System.out.println("Sorry, there are no " + guess + "'s\n");
				_guessesLeft--;
			}
			else {
				int count = 0;
				for (int i = 0; i < pat.length(); i++) {
					if (pat.charAt(i) != _VisibleWord.charAt(i)) {
						count++;
					}
				}
				System.out.print("Yes, there ");
				if (count == 1) {
					System.out.println("is 1 " + guess + "\n");
				}
				else if (count > 1) {
					System.out.println("are " + count + " " + guess + "'s\n");
				}
			}
		}
		_VisibleWord = pat;
	}
	
	public void decideGuess() {
		if (_guessesLeft != -1) {
			System.out.println("You have " + _guessesLeft +" guesses left");
			System.out.print("Used letters:");
			for (String s : _alreadyUsed) {
				System.out.print(" " + s);
			}
			System.out.println("\nWord: ­­­­­" + _VisibleWord);
			String current_guess = "@";
			Scanner user_input = new Scanner(System.in);
			while(true) {
				System.out.print("Enter guess: ");
				current_guess = user_input.next().toLowerCase();
				Pattern p = Pattern.compile("[a-zA-Z]");
				boolean alpha = p.matcher(current_guess).find();
				if (current_guess.length() != 1 || !alpha) {
					System.out.println("Invalid input\n");
				}
				else if (_alreadyUsed.contains(current_guess.toLowerCase())) {
					System.out.println("You already used that letter");
				}
				else {
					break;
				}
			}
			char c = current_guess.charAt(0);
			try {
				makeGuess(c);
				String underscore = "_";
				if (_AvailableWords.size() == 1 && !_VisibleWord.contains(underscore)) {
					//game over, user wins
					user_input.close();
					int count = 0;
					for(String s : _AvailableWords) {
						if (count == 0) {
							System.out.println("The word was: " + s);
						}	
						count++;
					}
					System.out.println("You win!");
				}
				else if (_guessesLeft < 1) {
					user_input.close();
					System.out.println("You lose!");
					int count = 0;
					for(String s : _AvailableWords) {
						if (count == 0) {
							System.out.println("The word was: " + s);
						}	
						count++;
					}
				}
				else {
					decideGuess();
				}
			}
			catch(GuessAlreadyMadeException ex) {
				System.out.println("GuessAlreadyMadeException!");
			}
		}
	}
	
}
