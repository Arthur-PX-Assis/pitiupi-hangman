
package pitiupi.hangman.ui;

import pitiupi.hangman.HangmanGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

public class HangmanPanel extends JPanel{

    public interface GameListener{
        void onWordSelected(String word);
        void onGuessSubmitted(char letter);
    }

    public enum PlayerRole{
        LOCAL, CHALLENGER, GUESSER
    }

    private final JButton startButton;
    private final JButton guessButton;
    private final JTextField letterField;

    private final JLabel wordLabel;
    private final JLabel errorsLabel;
    private final JLabel guessedLabel;
    private final JLabel statusLabel;
    private final JLabel waitingLabel;

    private final JTextArea drawingArea;

    private HangmanGame localGame;
    private PlayerRole role = PlayerRole.LOCAL;
    private GameListener listener;

    private boolean gameRunning;

    private static final String[] HANGMAN_STAGES ={
        " +---+\n |   |\n     |\n     |\n     |\n=========",
        " +---+\n |   |\n O   |\n     |\n     |\n=========",
        " +---+\n |   |\n O   |\n |   |\n     |\n=========",
        " +---+\n |   |\n O   |\n/|   |\n     |\n=========",
        " +---+\n |   |\n O   |\n/|\\  |\n     |\n=========",
        " +---+\n |   |\n O   |\n/|\\  |\n/    |\n=========",
        " +---+\n |   |\n O   |\n/|\\  |\n/ \\  |\n========="
    };

    public HangmanPanel(){
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Jogo da Forca", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        startButton = new JButton("Iniciar demonstração local");
        guessButton = new JButton("Enviar letra");
        letterField = new JTextField(5);

        wordLabel = new JLabel("_ _ _ _ _", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Monospaced", Font.BOLD, 30));

        errorsLabel = new JLabel("Erros: 0 / 6");
        guessedLabel = new JLabel("Letras tentadas: nenhuma");
        statusLabel = new JLabel("Escolha uma demonstração ou aguarde um convite.", SwingConstants.CENTER);

        waitingLabel = new JLabel("", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        drawingArea = new JTextArea(HANGMAN_STAGES[0]);
        drawingArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        drawingArea.setEditable(false);
        drawingArea.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(startButton, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));

        JPanel drawingPanel = new JPanel(new BorderLayout());
        drawingPanel.setBorder(BorderFactory.createTitledBorder("Forca"));
        drawingPanel.add(drawingArea, BorderLayout.CENTER);

        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        gamePanel.setBorder(BorderFactory.createTitledBorder("Partida"));

        wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        guessedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Letra:"));
        inputPanel.add(letterField);
        inputPanel.add(guessButton);

        gamePanel.add(Box.createVerticalStrut(20));
        gamePanel.add(waitingLabel);
        gamePanel.add(Box.createVerticalStrut(20));
        gamePanel.add(wordLabel);
        gamePanel.add(Box.createVerticalStrut(25));
        gamePanel.add(errorsLabel);
        gamePanel.add(Box.createVerticalStrut(15));
        gamePanel.add(guessedLabel);
        gamePanel.add(Box.createVerticalStrut(30));
        gamePanel.add(inputPanel);
        gamePanel.add(Box.createVerticalStrut(25));
        gamePanel.add(statusLabel);

        centerPanel.add(drawingPanel);
        centerPanel.add(gamePanel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        guessButton.setEnabled(false);
        letterField.setEnabled(false);

        startButton.addActionListener(e -> startLocalDemo());
        guessButton.addActionListener(e -> submitGuess());
        letterField.addActionListener(e -> submitGuess());
    }

    public void setGameListener(GameListener listener){
        this.listener = listener;
    }

    public void showChallengerScreen(){
        role = PlayerRole.CHALLENGER;
        gameRunning = false;

        startButton.setVisible(false);
        setGuessControlsEnabled(false);

        waitingLabel.setText("Você escolhe a palavra secreta.");
        wordLabel.setText("_ _ _ _ _");
        errorsLabel.setText("Erros: 0 / 6");
        guessedLabel.setText("Letras tentadas: nenhuma");
        drawingArea.setText(HANGMAN_STAGES[0]);
        statusLabel.setText("Aguardando escolha da palavra.");

        String word = JOptionPane.showInputDialog(
            this,
            "Digite a palavra que o adversário deverá descobrir:",
            "Escolher palavra",
            JOptionPane.PLAIN_MESSAGE
        );

        if (word == null){
            statusLabel.setText("Escolha da palavra cancelada.");
            return;
        }

        try{
            new HangmanGame(word);
        } catch (IllegalArgumentException exception){
            JOptionPane.showMessageDialog(
                this,
                "Digite uma palavra contendo apenas letras.",
                "Palavra inválida",
                JOptionPane.WARNING_MESSAGE
            );
            showChallengerScreen();
            return;
        }

        wordLabel.setText("_ ".repeat(word.trim().length()).trim());
        statusLabel.setText("Palavra escolhida. Aguardando palpites.");

        if (listener != null) listener.onWordSelected(word);
    }

    public void showGuesserWaitingScreen(String challengerName){
        role = PlayerRole.GUESSER;
        gameRunning = false;

        startButton.setVisible(false);
        setGuessControlsEnabled(false);

        waitingLabel.setText("⟳ Aguardando " + challengerName + " escolher a palavra...");
        wordLabel.setText("_ _ _ _ _");
        errorsLabel.setText("Erros: 0 / 6");
        guessedLabel.setText("Letras tentadas: nenhuma");
        drawingArea.setText(HANGMAN_STAGES[0]);
        statusLabel.setText("A partida começará em instantes.");

        revalidate();
        repaint();
    }

    public void startGuesserGame(){
        role = PlayerRole.GUESSER;
        gameRunning = true;

        waitingLabel.setText("");
        setGuessControlsEnabled(true);
        statusLabel.setText("Partida iniciada! Descubra a palavra.");
        letterField.requestFocusInWindow();
    }

    public void updateRemoteGame(
            String maskedWord,
            String guessedLetters,
            int errors,
            String status
    ){
        wordLabel.setText(maskedWord);
        guessedLabel.setText(
            guessedLetters == null || guessedLetters.isBlank()
                ? "Letras tentadas: nenhuma"
                : "Letras tentadas: " + guessedLetters
        );

        errorsLabel.setText("Erros: " + errors + " / " + HangmanGame.MAX_ERRORS);
        drawingArea.setText(HANGMAN_STAGES[Math.max(0, Math.min(errors, 6))]);
        statusLabel.setText(status);

        if ("WON".equals(status) || "LOST".equals(status)){
            gameRunning = false;
            setGuessControlsEnabled(false);
        }

        revalidate();
        repaint();
    }

    public void finishRemoteGame(String result, String secretWord){
        gameRunning = false;
        setGuessControlsEnabled(false);

        String message = "WON".equals(result)
            ? "Parabéns! Você venceu!"
            : "Fim de jogo! A palavra era: " + secretWord;

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
            this,
            message,
            "Fim de jogo",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void startLocalDemo(){
        String word = JOptionPane.showInputDialog(
            this,
            "Digite a palavra secreta:",
            "Nova demonstração",
            JOptionPane.PLAIN_MESSAGE
        );

        if (word == null) return;

        try{
            localGame = new HangmanGame(word);
        } catch (IllegalArgumentException exception){
            JOptionPane.showMessageDialog(
                this,
                "Digite uma palavra contendo apenas letras.",
                "Palavra inválida",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        role = PlayerRole.LOCAL;
        gameRunning = true;

        waitingLabel.setText("");
        setGuessControlsEnabled(true);
        statusLabel.setText("Partida em andamento!");

        updateLocalInterface();
        letterField.requestFocusInWindow();
    }

    private void submitGuess(){
        if (!gameRunning) return;

        String input = letterField.getText().trim();
        letterField.setText("");

        if (input.length() != 1 || !Character.isLetter(input.charAt(0))){
            statusLabel.setText("Digite apenas uma letra válida.");
            return;
        }

        char letter = input.charAt(0);

        if (role == PlayerRole.LOCAL) submitLocalGuess(letter);
        else if (role == PlayerRole.GUESSER && listener != null) listener.onGuessSubmitted(letter);

        letterField.requestFocusInWindow();
    }

    private void submitLocalGuess(char letter){
        HangmanGame.GuessResult result = localGame.guess(letter);

        switch (result){
            case CORRECT:
                statusLabel.setText("Boa! A letra existe na palavra.");
                break;
            case INCORRECT:
                statusLabel.setText("Essa letra não está na palavra.");
                break;
            case ALREADY_GUESSED:
                statusLabel.setText("Você já tentou essa letra.");
                break;
            case INVALID:
                statusLabel.setText("Digite uma letra válida.");
                break;
            case GAME_FINISHED:
                statusLabel.setText("A partida já terminou.");
                break;
        }

        updateLocalInterface();

        if (localGame.isWon()) finishLocalGame(true);
        else if (localGame.isLost()) finishLocalGame(false);
    }

    private void updateLocalInterface(){
        wordLabel.setText(localGame.getMaskedWord());
        errorsLabel.setText(
            "Erros: " + localGame.getErrors() + " / " + HangmanGame.MAX_ERRORS
        );

        Set<Character> letters = localGame.getGuessedLetters();

        String guessed = letters.isEmpty()
            ? "nenhuma"
            : letters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        guessedLabel.setText("Letras tentadas: " + guessed);
        drawingArea.setText(HANGMAN_STAGES[localGame.getErrors()]);
    }

    private void finishLocalGame(boolean won){
        gameRunning = false;
        setGuessControlsEnabled(false);

        String message = won
            ? "Parabéns! Você descobriu a palavra!"
            : "Fim de jogo! A palavra era: " + localGame.getSecretWord();

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
            this,
            message,
            won ? "Vitória!" : "Derrota!",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void setGuessControlsEnabled(boolean enabled){
        guessButton.setEnabled(enabled);
        letterField.setEnabled(enabled);
    }
}
