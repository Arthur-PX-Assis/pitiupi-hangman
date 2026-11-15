
package pitiupi.hangman.messages;

import pitiupi.models.Messages.Message;

public class HangmanInviteResponseMessage extends Message{

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String fromInstanceId;
    private String fromDisplayName;
    private String toInstanceId;
    private boolean accepted;

    public HangmanInviteResponseMessage(){
        super(true);
    }

    public HangmanInviteResponseMessage(
            String gameId,
            String fromInstanceId,
            String fromDisplayName,
            String toInstanceId,
            boolean accepted
    ){
        super(true);
        this.gameId = gameId;
        this.fromInstanceId = fromInstanceId;
        this.fromDisplayName = fromDisplayName;
        this.toInstanceId = toInstanceId;
        this.accepted = accepted;
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

    public String getFromDisplayName(){
        return fromDisplayName;
    }

    public void setFromDisplayName(String fromDisplayName){
        this.fromDisplayName = fromDisplayName;
    }

    public String getToInstanceId(){
        return toInstanceId;
    }

    public void setToInstanceId(String toInstanceId){
        this.toInstanceId = toInstanceId;
    }

    public boolean isAccepted(){
        return accepted;
    }

    public void setAccepted(boolean accepted){
        this.accepted = accepted;
    }
}
