
package pitiupi.hangman;

public class HangmanGameTest {

    private static int testsPassed = 0;

    public static void main(String[] args) {

        testInitialWord();
        testCorrectGuess();
        testIncorrectGuess();
        testRepeatedGuess();
        testGameLost();
        testGameWon();
        testAccentedWord();
        testInvalidGuess();

        System.out.println();
        System.out.println("================================");
        System.out.println("Testes concluídos: " + testsPassed);
        System.out.println("Todos os testes passaram!");
        System.out.println("================================");
    }

    private static void testInitialWord() {
        HangmanGame game = new HangmanGame("BANANA");

        check(
            game.getMaskedWord().equals("_ _ _ _ _ _"),
            "Palavra começa oculta"
        );
    }

    private static void testCorrectGuess() {
        HangmanGame game = new HangmanGame("BANANA");

        HangmanGame.GuessResult result = game.guess('A');

        check(
            result == HangmanGame.GuessResult.CORRECT,
            "Letra correta reconhecida"
        );

        check(
            game.getMaskedWord().equals("_ A _ A _ A"),
            "Todas as ocorrências da letra são reveladas"
        );

        check(
            game.getErrors() == 0,
            "Letra correta não aumenta os erros"
        );
    }

    private static void testIncorrectGuess() {
        HangmanGame game = new HangmanGame("BANANA");

        HangmanGame.GuessResult result = game.guess('Z');

        check(
            result == HangmanGame.GuessResult.INCORRECT,
            "Letra incorreta reconhecida"
        );

        check(
            game.getErrors() == 1,
            "Erro contabilizado corretamente"
        );
    }

    private static void testRepeatedGuess() {
        HangmanGame game = new HangmanGame("BANANA");

        game.guess('Z');

        HangmanGame.GuessResult result = game.guess('Z');

        check(
            result == HangmanGame.GuessResult.ALREADY_GUESSED,
            "Letra repetida identificada"
        );

        check(
            game.getErrors() == 1,
            "Letra repetida não aumenta os erros"
        );
    }

    private static void testGameLost() {
        HangmanGame game = new HangmanGame("BANANA");

        game.guess('X');
        game.guess('Y');
        game.guess('Z');
        game.guess('W');
        game.guess('K');

        check(
            game.getStatus() == HangmanGame.GameStatus.IN_PROGRESS,
            "Partida continua após cinco erros"
        );

        game.guess('J');

        check(
            game.getErrors() == 6,
            "Limite de seis erros atingido"
        );

        check(
            game.isLost(),
            "Derrota identificada"
        );

        check(
            game.guess('A') == HangmanGame.GuessResult.GAME_FINISHED,
            "Não permite palpites após o fim"
        );
    }

    private static void testGameWon() {
        HangmanGame game = new HangmanGame("SOL");

        game.guess('S');
        game.guess('O');
        game.guess('L');

        check(
            game.isWon(),
            "Vitória identificada"
        );

        check(
            game.getStatus() == HangmanGame.GameStatus.WON,
            "Estado da partida atualizado para vitória"
        );
    }

    private static void testAccentedWord() {
        HangmanGame game = new HangmanGame("MAÇÃ");

        check(
            game.getMaskedWord().equals("_ _ _ _"),
            "Palavra com acentos começa oculta"
        );

        game.guess('c');
        game.guess('a');
        game.guess('m');

        check(
            game.isWon(),
            "Palavra com acentos é normalizada corretamente"
        );
    }

    private static void testInvalidGuess() {
        HangmanGame game = new HangmanGame("BANANA");

        check(
            game.guess('1') == HangmanGame.GuessResult.INVALID,
            "Números são rejeitados"
        );

        check(
            game.getErrors() == 0,
            "Palpite inválido não conta como erro"
        );
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError("FALHOU: " + description);
        }

        testsPassed++;
        System.out.println("[OK] " + description);
    }
}
