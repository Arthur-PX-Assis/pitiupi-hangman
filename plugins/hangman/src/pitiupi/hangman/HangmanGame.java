
package pitiupi.hangman;

import java.text.Normalizer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class HangmanGame{

    public static final int MAX_ERRORS = 6;

    public enum GameStatus{
        IN_PROGRESS,
        WON,
        LOST
    }

    public enum GuessResult{
        CORRECT,
        INCORRECT,
        ALREADY_GUESSED,
        GAME_FINISHED,
        INVALID
    }

    private final String secretWord;
    private final Set<Character> guessedLetters;

    private int errors;
    private GameStatus status;

    public HangmanGame(String word){
        String normalizedWord = normalize(word);

        if (normalizedWord.isEmpty() || !normalizedWord.matches("\\p{L}+"))
            throw new IllegalArgumentException("A palavra deve conter apenas letras.");

        this.secretWord = normalizedWord;
        this.guessedLetters = new LinkedHashSet<>();
        this.errors = 0;
        this.status = GameStatus.IN_PROGRESS;
    }

    public GuessResult guess(char letter){

        if (status != GameStatus.IN_PROGRESS) return GuessResult.GAME_FINISHED;

        String normalizedLetter = normalize(String.valueOf(letter));

        if (normalizedLetter.length() != 1 || !Character.isLetter(normalizedLetter.charAt(0)))
            return GuessResult.INVALID;

        char normalizedChar = normalizedLetter.charAt(0);

        if (guessedLetters.contains(normalizedChar)) return GuessResult.ALREADY_GUESSED;

        guessedLetters.add(normalizedChar);

        if (secretWord.indexOf(normalizedChar) >= 0){
            if (hasWon()) status = GameStatus.WON;

            return GuessResult.CORRECT;
        }

        errors++;

        if (errors >= MAX_ERRORS) status = GameStatus.LOST;

        return GuessResult.INCORRECT;
    }

    public String getMaskedWord(){
        StringBuilder maskedWord = new StringBuilder();

        for (int i = 0; i < secretWord.length(); i++){
            char letter = secretWord.charAt(i);

            if (guessedLetters.contains(letter)) maskedWord.append(letter);
            else maskedWord.append('_');

            if (i < secretWord.length() - 1) maskedWord.append(' ');
        }

        return maskedWord.toString();
    }

    private boolean hasWon(){
        for (char letter : secretWord.toCharArray())
            if (!guessedLetters.contains(letter)) return false;

        return true;
    }

    private String normalize(String value){
        if (value == null) return "";

        String normalized = Normalizer.normalize(
                value.trim(),
                Normalizer.Form.NFD
        );

        return normalized
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    public int getErrors(){
        return errors;
    }

    public int getRemainingAttempts(){
        return MAX_ERRORS - errors;
    }

    public GameStatus getStatus(){
        return status;
    }

    public boolean isFinished(){
        return status != GameStatus.IN_PROGRESS;
    }

    public boolean isWon(){
        return status == GameStatus.WON;
    }

    public boolean isLost(){
        return status == GameStatus.LOST;
    }

    public Set<Character> getGuessedLetters(){
        return Collections.unmodifiableSet(guessedLetters);
    }

    public String getSecretWord(){
        return secretWord;
    }
}
