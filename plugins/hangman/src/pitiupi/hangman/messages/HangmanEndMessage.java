
package pitiupi.hangman.messages;

import pitiupi.models.Messages.Message;

public class HangmanEndMessage extends Message{

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String fromInstanceId;
    private String toInstanceId;
    private String result;
    private String secretWord;

    public HangmanEndMessage(){
        super(true);
    }

    public HangmanEndMessage(
            String gameId,
            String fromInstanceId,
            String toInstanceId,
            String result,
            String secretWord
    ){
        super(true);
        this.gameId = gameId;
        this.fromInstanceId = fromInstanceId;
        this.toInstanceId = toInstanceId;
        this.result = result;
        this.secretWord = secretWord;
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

    public String getResult(){
        return result;
    }

    public void setResult(String result){
        this.result = result;
    }

    public String getSecretWord(){
        return secretWord;
    }

    public void setSecretWord(String secretWord){
        this.secretWord = secretWord;
    }
}
