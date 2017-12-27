package hangman;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import hangman.IEvilHangmanGame.GuessAlreadyMadeException;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		int wordLength = Integer.parseInt(args[1]);
		int guesses = Integer.parseInt(args[2]);
		File dicFile = new File(args[0]);
		EvilHangmanGame game = new EvilHangmanGame();
		game.startGame(dicFile, wordLength);
		game.setGuesses(guesses);
		game.decideGuess();
	}
	
}