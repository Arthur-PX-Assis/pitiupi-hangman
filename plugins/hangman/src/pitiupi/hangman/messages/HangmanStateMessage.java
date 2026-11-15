
package pitiupi.hangman.messages;

import pitiupi.models.Messages.Message;

public class HangmanStateMessage extends Message{

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String maskedWord;
    private String guessedLetters;
    private int errors;
    private String status;

    public HangmanStateMessage(){
        super(true);
    }

    public HangmanStateMessage(
            String gameId,
            String maskedWord,
            String guessedLetters,
            int errors,
            String status
    ){
        super(true);
        this.gameId = gameId;
        this.maskedWord = maskedWord;
        this.guessedLetters = guessedLetters;
        this.errors = errors;
        this.status = status;
    }

    public String getGameId(){
        return gameId;
    }

    public void setGameId(String gameId){
        this.gameId = gameId;
    }

    public String getMaskedWord(){
        return maskedWord;
    }

    public void setMaskedWord(String maskedWord){
        this.maskedWord = maskedWord;
    }

    public String getGuessedLetters(){
        return guessedLetters;
    }

    public void setGuessedLetters(String guessedLetters){
        this.guessedLetters = guessedLetters;
    }

    public int getErrors(){
        return errors;
    }

    public void setErrors(int errors){
        this.errors = errors;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
