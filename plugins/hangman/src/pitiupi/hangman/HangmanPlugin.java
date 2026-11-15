
package pitiupi.hangman;

import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.api.plugin.InviteDetails;
import pitiupi.application.invites.SessionInviteKind;
import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.application.presence.UserPeerView;
import pitiupi.control.ConnectionManager;
import pitiupi.hangman.messages.HangmanEndMessage;
import pitiupi.hangman.messages.HangmanGuessMessage;
import pitiupi.hangman.messages.HangmanInviteResponseMessage;
import pitiupi.hangman.messages.HangmanStartMessage;
import pitiupi.hangman.messages.HangmanStateMessage;
import pitiupi.hangman.ui.HangmanPanel;
import pitiupi.models.Messages.Message;
import pitiupi.models.Messages.SessionInviteMessage;
import pitiupi.models.Plugin;
import pitiupi.control.PeerListener;
import pitiupi.models.User;

import javax.swing.JOptionPane;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HangmanPlugin extends Plugin implements PeerListener{

    private enum Role{
        NONE,
        CHALLENGER,
        GUESSER
    }

    private ConnectionManager connectionManager;
    private PluginWorkspace workspace;
    private HangmanPanel panel;

    private String pendingGameId;
    private String pendingPeerInstanceId;

    private String activeGameId;
    private String activeOpponentId;
    private String activeOpponentName;

    private Role activeRole = Role.NONE;
    private HangmanGame game;

    private final Map<String, Integer> lastPromptedInviteCount = new ConcurrentHashMap<>();

    @Override
    public void createPlugin(
            ConnectionManager connectionManager,
            PluginWorkspace workspace
    ){
        this.connectionManager = connectionManager;
        this.workspace = workspace;

        panel = new HangmanPanel();

        panel.setGameListener(new HangmanPanel.GameListener(){
            @Override
            public void onWordSelected(String word){
                startGameAsChallenger(word);
            }

            @Override
            public void onGuessSubmitted(char letter){
                submitGuess(letter);
            }
        });

        bindShellWorkspaceContent(panel);
        installPeerActions(workspace.peerActions());
        connectionManager.addPeerListener(this);
    }

    @Override
    public void installPeerActions(PeerActionsRegistrar registrar){
        registrar.register(PeerAction.of(
                "hangman.invite",
                "Convidar para jogar Forca",
                PeerActionTarget.SINGLE_PEER,
                this::invitePeer
        ));
    }

    private void invitePeer(PeerTarget target){
        if (!(target instanceof PeerTarget.Single single)) return;

        UserPeerView peer = single.peer();

        if (peer == null || peer.isSelf() || peer.isBroadcastTarget()) return;

        if (peer.getInstanceId() == null || peer.getInetAddress().isEmpty()){
            workspace.setStatusMessage("Não foi possível obter o endereço desse jogador.");
            return;
        }

        String gameId = UUID.randomUUID().toString();

        SessionInviteMessage invite = new SessionInviteMessage(
                UUID.randomUUID().toString(),
                getLocalInstanceId(),
                connectionManager.getUserName(),
                peer.getInstanceId(),
                getIdentifier(),
                SessionInviteKind.CUSTOM,
                gameId
        );

        try{
            int sent = MessageDispatcher.send(
                    connectionManager,
                    invite,
                    SendTarget.singlePeer(peer.getInetAddress().get()),
                    TransportMode.UNICAST_UDP
            );

            if (sent <= 0){
                workspace.setStatusMessage("Não foi possível enviar o convite.");
                return;
            }

            pendingGameId = gameId;
            pendingPeerInstanceId = peer.getInstanceId();

            workspace.setStatusMessage("Convite enviado para " + peer.getDisplayName() + ".");

        } catch (Exception ex){
            workspace.setStatusMessage("Erro ao enviar convite: " + ex.getMessage());
        }
    }

    @Override
    public void onInviteAccepted(InviteDetails invite){
        if (invite == null || !getIdentifier().equals(invite.getPluginId())) return;

        String gameId = invite.getPayload();

        if (gameId == null || gameId.isBlank()){
            workspace.setStatusMessage("Convite de jogo inválido.");
            return;
        }

        activeGameId = gameId;
        activeOpponentId = invite.getFromInstanceId();
        activeOpponentName = invite.getFromDisplayName();
        activeRole = Role.GUESSER;
        game = null;

        openGameTab();

        SwingUtilities.invokeLater(() ->
                panel.showGuesserWaitingScreen(activeOpponentName)
        );

        HangmanInviteResponseMessage response =
                new HangmanInviteResponseMessage(
                        gameId,
                        getLocalInstanceId(),
                        connectionManager.getUserName(),
                        activeOpponentId,
                        true
                );

        if (!sendToPeer(response, activeOpponentId))
            workspace.setStatusMessage("Convite aceito, mas não foi possível confirmar ao desafiante.");
    }

    @Override
    public List<Class<? extends Message>> getMessages(){
        return List.of(
                HangmanInviteResponseMessage.class,
                HangmanStartMessage.class,
                HangmanGuessMessage.class,
                HangmanStateMessage.class,
                HangmanEndMessage.class
        );
    }

    @Override
    public void receiveMessage(Message message){
        if (message == null) return;

        SwingUtilities.invokeLater(() ->{
            if (message instanceof HangmanInviteResponseMessage response) receiveInviteResponse(response);
            else if (message instanceof HangmanStartMessage start) receiveGameStart(start);
            else if (message instanceof HangmanGuessMessage guess) receiveGuess(guess);
            else if (message instanceof HangmanStateMessage state) receiveGameState(state);
            else if (message instanceof HangmanEndMessage end) receiveGameEnd(end);
        });
    }

    private void receiveInviteResponse(HangmanInviteResponseMessage response){
        if (pendingGameId == null
                || !pendingGameId.equals(response.getGameId())
                || !pendingPeerInstanceId.equals(response.getFromInstanceId())
                || !getLocalInstanceId().equals(response.getToInstanceId())){
            return;
        }

        if (!response.isAccepted()){
            workspace.setStatusMessage("O convite foi recusado.");
            clearPendingInvite();
            return;
        }

        activeGameId = pendingGameId;
        activeOpponentId = response.getFromInstanceId();
        activeOpponentName = response.getFromDisplayName();
        activeRole = Role.CHALLENGER;
        game = null;

        clearPendingInvite();
        openGameTab();

        SwingUtilities.invokeLater(panel::showChallengerScreen);
    }

    private void startGameAsChallenger(String word){
        if (activeRole != Role.CHALLENGER || activeGameId == null) return;

        try{
            game = new HangmanGame(word);

            HangmanStartMessage start = new HangmanStartMessage(
                    activeGameId,
                    getLocalInstanceId(),
                    activeOpponentId,
                    connectionManager.getUserName(),
                    game.getSecretWord()
            );

            if (!sendToPeer(start, activeOpponentId)){
                workspace.setStatusMessage("Não foi possível iniciar a partida com o outro jogador.");
                return;
            }

            updatePanelFromGame();
            sendGameState();

        } catch (IllegalArgumentException ex){
            workspace.setStatusMessage("A palavra informada não é válida.");
        }
    }

    private void receiveGameStart(HangmanStartMessage start){
        if (activeRole != Role.GUESSER
                || !matchesActiveGame(start.getGameId())
                || !getLocalInstanceId().equals(start.getGuesserInstanceId())
                || !activeOpponentId.equals(start.getChallengerInstanceId())){
            return;
        }

        activeOpponentName = start.getChallengerDisplayName();

        panel.startGuesserGame();

        workspace.setStatusMessage( "A partida começou contra " + activeOpponentName + ".");
    }

    private void submitGuess(char letter){
        if (activeRole != Role.GUESSER || activeGameId == null){
            return;
        }

        HangmanGuessMessage guess = new HangmanGuessMessage(
                activeGameId,
                getLocalInstanceId(),
                activeOpponentId,
                Character.toUpperCase(letter)
        );

        if (!sendToPeer(guess, activeOpponentId))
            workspace.setStatusMessage( "Não foi possível enviar seu palpite.");
    }

    private void receiveGuess(HangmanGuessMessage guess){
        if (activeRole != Role.CHALLENGER
                || game == null
                || !matchesActiveGame(guess.getGameId())
                || !activeOpponentId.equals(guess.getFromInstanceId())
                || !getLocalInstanceId().equals(guess.getToInstanceId())
                || game.isFinished()){
            return;
        }

        game.guess(guess.getLetter());

        updatePanelFromGame();
        sendGameState();

        if (game.isFinished()) sendGameEnd();
    }

    private void sendGameState(){
        if (game == null || activeOpponentId == null) return;

        String guessedLetters = game.getGuessedLetters()
                .stream()
                .map(String::valueOf)
                .sorted()
                .collect(Collectors.joining(", "));

        HangmanStateMessage state = new HangmanStateMessage(
                activeGameId,
                game.getMaskedWord(),
                guessedLetters,
                game.getErrors(),
                game.getStatus().name()
        );

        sendToPeer(state, activeOpponentId);
    }

    private void receiveGameState(HangmanStateMessage state){
        if (activeRole != Role.GUESSER || !matchesActiveGame(state.getGameId())) return;

        panel.updateRemoteGame(
                state.getMaskedWord(),
                state.getGuessedLetters(),
                state.getErrors(),
                state.getStatus()
        );
    }

    private void sendGameEnd(){
        if (game == null || activeOpponentId == null) return;

        String result = game.isWon()
                ? "GUESSER_WON"
                : "CHALLENGER_WON";

        HangmanEndMessage end = new HangmanEndMessage(
                activeGameId,
                getLocalInstanceId(),
                activeOpponentId,
                result,
                game.getSecretWord()
        );

        sendToPeer(end, activeOpponentId);

        String localResult = game.isWon() ? "LOST" : "WON";

        panel.finishRemoteGame(
                localResult,
                game.getSecretWord()
        );
    }

    private void receiveGameEnd(HangmanEndMessage end){
        if (activeRole != Role.GUESSER
                || !matchesActiveGame(end.getGameId())
                || !activeOpponentId.equals(end.getFromInstanceId())
                || !getLocalInstanceId().equals(end.getToInstanceId())){
            return;
        }

        String localResult = "GUESSER_WON".equals(end.getResult())
                ? "WON"
                : "LOST";

        panel.finishRemoteGame(
                localResult,
                end.getSecretWord()
        );

        activeRole = Role.NONE;
    }

    private void updatePanelFromGame(){
        if (game == null) return;

        String guessedLetters = game.getGuessedLetters()
                .stream()
                .map(String::valueOf)
                .sorted()
                .collect(Collectors.joining(", "));

        panel.updateRemoteGame(
                game.getMaskedWord(),
                guessedLetters,
                game.getErrors(),
                game.getStatus().name()
        );
    }

    private boolean sendToPeer(Message message, String peerInstanceId){
        UserPeerView peer = findPeer(peerInstanceId);

        if (peer == null || peer.getInetAddress().isEmpty()) return false;

        try{
            return MessageDispatcher.send(
                    connectionManager,
                    message,
                    SendTarget.singlePeer(peer.getInetAddress().get()),
                    TransportMode.UNICAST_UDP
            ) > 0;

        } catch (Exception ex){
            System.out.println( "Erro ao enviar mensagem do Hangman: " + ex.getMessage());
            return false;
        }
    }

    private UserPeerView findPeer(String instanceId){
        if (instanceId == null) return null;

        return connectionManager.getPresenceService()
                .listPeerViews()
                .stream()
                .filter(peer -> !peer.isSelf())
                .filter(peer -> !peer.isBroadcastTarget())
                .filter(peer -> instanceId.equals(peer.getInstanceId()))
                .findFirst()
                .orElse(null);
    }

    private void openGameTab(){
        workspace.openOrFocusTab(
                getIdentifier(),
                getDisplayName()
        );
    }

    private String getLocalInstanceId(){
        return connectionManager.getHeartbeatManager().getInstanceId();
    }

    private boolean matchesActiveGame(String gameId){
        return activeGameId != null && activeGameId.equals(gameId);
    }

    private void clearPendingInvite(){
        pendingGameId = null;
        pendingPeerInstanceId = null;
    }

    @Override
    public String getName(){
        return "Hangman";
    }

    @Override
    public String getDisplayName(){
        return "Jogo da Forca";
    }

    @Override
    public String getAuthor(){
        return connectionManager != null
                ? connectionManager.getUserName()
                : "Pitiupi Hangman";
    }

    @Override
    public String getVersion(){
        return "1.0";
    }

    @Override
    public String getIdentifier(){
        return "Pitiupi.Hangman.Example";
    }

    @Override
    public void onPeersChanged(List<User> peers){
        SwingUtilities.invokeLater(() ->{
            for (User peer : peers){
                String instanceId = peer.getInstanceId();

                if (instanceId == null || instanceId.isBlank()) continue;

                int pendingCount = connectionManager
                        .getInviteInboxService()
                        .getNotificationCount(instanceId);

                int promptedCount = lastPromptedInviteCount
                        .getOrDefault(instanceId, 0);

                if (pendingCount < promptedCount){
                    lastPromptedInviteCount.put(instanceId, pendingCount);
                    promptedCount = pendingCount;
                }

                if (pendingCount <= promptedCount) continue;

                lastPromptedInviteCount.put(instanceId, pendingCount);

                int option = JOptionPane.showConfirmDialog(
                        null,
                        peer.getUserName()
                                + " enviou um convite de jogo.\n\n"
                                + "Deseja aceitar?",
                        "Convite para jogar Forca",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION){
                    String acceptedPluginId = connectionManager
                            .getInviteInboxService()
                            .acceptFirstInviteFrom(instanceId);

                    if (getIdentifier().equals(acceptedPluginId)) connectionManager.notifyInviteAcceptedForUi(acceptedPluginId);
                    else if (acceptedPluginId == null) workspace.setStatusMessage( "Não foi possível localizar o convite.");
                    else workspace.setStatusMessage( "O convite recebido pertence a outro plugin.");
                }
            }
        });
    }
}
