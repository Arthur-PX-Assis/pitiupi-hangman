
package pitiupi.hangman.messages;

import pitiupi.models.Messages.Message;

public class HangmanGuessMessage extends Message{

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String fromInstanceId;
    private String toInstanceId;
    private char letter;

    public HangmanGuessMessage(){
        super(true);
    }

    public HangmanGuessMessage(
            String gameId,
            String fromInstanceId,
            String toInstanceId,
            char letter
    ){
        super(true);
        this.gameId = gameId;
        this.fromInstanceId = fromInstanceId;
        this.toInstanceId = toInstanceId;
        this.letter = Character.toUpperCase(letter);
    }

    public String getGameId(){
        return gameId;
    }

    public void setGameId(String gameId){
        this.gameId = gameId;
    }

    public String getFromInstanceId(){
        return fromInstanceId;
    }

    public void setFromInstanceId(String fromInstanceId){
        this.fromInstanceId = fromInstanceId;
    }

    public String getToInstanceId(){
        return toInstanceId;
    }

    public void setToInstanceId(String toInstanceId){
        this.toInstanceId = toInstanceId;
    }

    public char getLetter(){
        return letter;
    }

    public void setLetter(char letter){
        this.letter = Character.toUpperCase(letter);
    }
}
