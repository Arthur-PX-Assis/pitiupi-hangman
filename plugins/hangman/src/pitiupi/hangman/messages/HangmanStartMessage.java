
package pitiupi.hangman.messages;

import pitiupi.models.Messages.Message;

public class HangmanStartMessage extends Message{

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String challengerInstanceId;
    private String guesserInstanceId;
    private String challengerDisplayName;
    private String secretWord;

    public HangmanStartMessage(){
        super(true);
    }

    public HangmanStartMessage(
            String gameId,
            String challengerInstanceId,
            String guesserInstanceId,
            String challengerDisplayName,
            String secretWord
    ){
        super(true);
        this.gameId = gameId;
        this.challengerInstanceId = challengerInstanceId;
        this.guesserInstanceId = guesserInstanceId;
        this.challengerDisplayName = challengerDisplayName;
        this.secretWord = secretWord;
    }

    public String getGameId(){
        return gameId;
    }

    public void setGameId(String gameId){
        this.gameId = gameId;
    }

    public String getChallengerInstanceId(){
        return challengerInstanceId;
    }

    public void setChallengerInstanceId(String challengerInstanceId){
        this.challengerInstanceId = challengerInstanceId;
    }

    public String getGuesserInstanceId(){
        return guesserInstanceId;
    }

    public void setGuesserInstanceId(String guesserInstanceId){
        this.guesserInstanceId = guesserInstanceId;
    }

    public String getChallengerDisplayName(){
        return challengerDisplayName;
    }

    public void setChallengerDisplayName(String challengerDisplayName){
        this.challengerDisplayName = challengerDisplayName;
    }

    public String getSecretWord(){
        return secretWord;
    }

    public void setSecretWord(String secretWord){
        this.secretWord = secretWord;
    }
}
