package xyz.niflheim.stockfish.engine;

import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import xyz.niflheim.stockfish.engine.enums.Variant;
import xyz.niflheim.stockfish.exceptions.StockfishEngineException;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.niflheim.stockfish.util.ProcessManager.getProcessNumber;
import static xyz.niflheim.stockfish.util.StringUtil.*;

class StockfishTest {

    private static final Pattern fenPattern = Pattern.compile(START_REGEX + FEN_REGEX + END_REGEX);
    private static final Logger log = LogManager.getLogger(StockfishTest.class);
    private Stockfish stockfish;

    @BeforeEach
    void setUp() {
        try {
            stockfish = new Stockfish(
                new File("assets/engines/" + Variant.DEFAULT.fileName(
                    System.getProperty("os.name").toLowerCase().contains("win"),
                    "15.1",
                    false
                )).toPath(),
                Map.of()
            );
        } catch (StockfishInitException e) {
            log.error("error while create Stockfish client: ", e);
            fail(e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            stockfish.close();
        } catch (IOException | StockfishEngineException e) {
            log.error("error while close Stockfish client: ", e);
        }
    }

    @Disabled("Not updated for removal of multi-instance client")
    @RepeatedTest(10)
    void createCorrectly() {
        try {
            assertEquals(1, getProcessNumber());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void incorrectCommand() {
        try {
            String incorrectCommand = "incorrect command";
            stockfish.sendCommand(incorrectCommand);
            assertEquals(List.of(ERROR_STOCKFISH + "'incorrect command'. Type help for more information."),
                    stockfish.readResponse(ERROR_STOCKFISH));

            incorrectCommand = "one more incorrect command";
            stockfish.sendCommand(incorrectCommand);
            assertEquals(List.of(ERROR_STOCKFISH + "'one more incorrect command'. Type help for more information."),
                    stockfish.readResponse(ERROR_STOCKFISH));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void waitForReady() {
        try {
            assertDoesNotThrow(() -> stockfish.waitForReady());
            stockfish.process.destroy();
            assertThrows(StockfishEngineException.class, () -> stockfish.waitForReady());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void sendCommand() {
        try {
            File tempFile = creteTempFile();
            setOutput(tempFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(tempFile.toPath())));
            stockfish.sendCommand("hello world");
            assertEquals("hello world", reader.readLine());
            stockfish.sendCommand("hello world1");
            assertEquals("hello world1", reader.readLine());
            stockfish.sendCommand("hello world2");
            assertEquals("hello world2", reader.readLine());
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    void readLine() {
        try {
            File tempFile = creteTempFile();
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            outputStream.write("31\n".getBytes());
            for (int i = 0; i < 33; i++) {
                outputStream.write((i + "\n").getBytes());
            }
            outputStream.flush();
            setInput(tempFile);

            assertEquals("31", stockfish.readLine("3"));
            assertEquals("1", stockfish.readLine("1"));
            assertEquals("3", stockfish.readLine("3"));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine("40"));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine("21"));
            outputStream.write("21\n".getBytes());
            outputStream.flush();
            assertEquals("21", stockfish.readLine("21"));

        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    void readResponse() {
        try {
            File tempFile = creteTempFile();
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile, true));
            List<String> response = new ArrayList<>();
            outputStream.write("31\n".getBytes());
            for (int i = 0; i < 33; i++) {
                response.add(Integer.toString(i));
                outputStream.write((i + "\n").getBytes());
            }
            outputStream.flush();
            setInput(tempFile);

            assertArrayEquals(new String[]{"31"}, stockfish.readResponse("31").toArray());
            assertArrayEquals(response.toArray(), stockfish.readResponse("32").toArray());
            assertThrows(StockfishEngineException.class, () -> stockfish.readResponse("36").toArray());
            assertThrows(StockfishEngineException.class, () -> stockfish.readResponse("31").toArray());

            outputStream.write("31\n".getBytes());
            outputStream.flush();

            assertArrayEquals(new String[]{"31"}, stockfish.readResponse("31").toArray());
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {

            fail(e);
        }
    }

    @Test
    void makeMove() {
        try {
            Query<String> makeMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN)
                    .setMoves("a2a4").setMovetime(1000)
                    .build();
            log.info("Move: " + stockfish.makeMoves(makeMove));
            final Matcher matcher = fenPattern.matcher(stockfish.makeMoves(makeMove));
            assertTrue(matcher.matches());
            makeMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN)
                    .setMoves("a2h6")
                    .build();
            assertEquals(START_FEN, stockfish.makeMoves(makeMove));
            makeMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN)
                    .build();
            assertEquals(START_FEN, stockfish.makeMoves(makeMove));
            assertEquals(START_FEN, stockfish.makeMoves(makeMove));
            Query<String> makeErrorMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b KQkq - 0 1")
                    .build();

            assertThrows(StockfishEngineException.class, () -> stockfish.makeMoves(makeErrorMove));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine(""));
            assertFalse(stockfish.process.isAlive());
            assertEquals(139, stockfish.process.exitValue());
        } catch (Exception e) {
            fail(e);
        }

    }

    @Test
    void getCheckers() {
        try {
//            Query checkersQuery = new Query.Builder(QueryType.Checkers, START_FEN).build();
//            assertEquals("", stockfish.getCheckers(checkersQuery));

            Query<String> makeErrorMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b KQkq - 0 1")
                    .build();

            assertThrows(StockfishEngineException.class, () -> stockfish.makeMoves(makeErrorMove));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine(""));
            assertFalse(stockfish.process.isAlive());
            assertEquals(139, stockfish.process.exitValue());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void getBestMove() {
        try {
            final String move = "^([a-h][1-9]){2}$";
            final Pattern movePattern = Pattern.compile(move);
            Query<String> bestMoveQuery = new Query.Builder<>(QueryTypes.BEST_MOVE, START_FEN).setDepth(10).build();
            String bestMove = stockfish.getBestMove(bestMoveQuery);
            log.info(bestMove);
            assertTrue(movePattern.matcher(bestMove).matches());

            bestMoveQuery = new Query.Builder<>(QueryTypes.BEST_MOVE, START_FEN)
                    .setDepth(10)
                    .setMovetime(10)
                    .setDifficulty(10)
                    .build();
            bestMove = stockfish.getBestMove(bestMoveQuery);
            log.info(bestMove);
            assertTrue(movePattern.matcher(bestMove).matches());

            bestMoveQuery = new Query.Builder<>(QueryTypes.BEST_MOVE, START_FEN)
                    .setDepth(10) // todo
                    .setMovetime(-10)
                    .setDifficulty(-10)
                    .build();
            bestMove = stockfish.getBestMove(bestMoveQuery);
            log.info(bestMove);
            assertTrue(movePattern.matcher(bestMove).matches());

            Query makeErrorMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b KQkq - 0 1")
                    .build();

            assertThrows(StockfishEngineException.class, () -> stockfish.makeMoves(makeErrorMove));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine(""));
            assertFalse(stockfish.process.isAlive());
            assertEquals(139, stockfish.process.exitValue());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void getLegalMoves() {
        try {
            final String legalMovesRegex = "^(([a-h][1-9]){2}\\s)+$";
            final Pattern movePattern = Pattern.compile(legalMovesRegex);
            Query<Set<String>> legalMoveQuery = new Query.Builder<>(QueryTypes.LEGAL_MOVES, START_FEN).build();
            Set<String> legalMoves = stockfish.getLegalMoves(legalMoveQuery);
            log.info(legalMoves);
            assertTrue(movePattern.matcher(String.join(" ", legalMoves) + " ").matches());
            legalMoveQuery = new Query.Builder<>(QueryTypes.LEGAL_MOVES, START_FEN)
                    .setDepth(20)
                    .setMovetime(20)
                    .setDifficulty(20)
                    .build();
            legalMoves = stockfish.getLegalMoves(legalMoveQuery);
            log.info(legalMoves);
            assertTrue(movePattern.matcher(String.join(" ", legalMoves) + " ").matches());

            legalMoveQuery = new Query.Builder<>(QueryTypes.LEGAL_MOVES, START_FEN)
                    .setDepth(-10)
                    .setMovetime(-10)
                    .setDifficulty(-10)
                    .build();
            legalMoves = stockfish.getLegalMoves(legalMoveQuery);
            log.info(legalMoves);
            assertTrue(movePattern.matcher(String.join(" ", legalMoves) + " ").matches());

            Query<String> makeErrorMove = new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b KQkq - 0 1")
                    .build();

            assertThrows(StockfishEngineException.class, () -> stockfish.makeMoves(makeErrorMove));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine(""));
            assertFalse(stockfish.process.isAlive());
            assertEquals(139, stockfish.process.exitValue());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void checkExceptionAfterClose() {
        try {
            stockfish.process.destroy();
//            assertThrows(IOException.class, () -> stockfish.close());
            assertThrows(StockfishEngineException.class, () -> stockfish.waitForReady());
            assertThrows(StockfishEngineException.class, () -> stockfish.sendCommand(""));
            assertThrows(StockfishEngineException.class,
                    () -> stockfish.getBestMove(new Query.Builder<>(QueryTypes.BEST_MOVE, START_FEN).build()));
            assertThrows(StockfishEngineException.class,
                    () -> stockfish.getCheckers(new Query.Builder<>(QueryTypes.CHECKERS, START_FEN).build()));
            assertThrows(StockfishEngineException.class,
                    () -> stockfish.getLegalMoves(new Query.Builder<>(QueryTypes.LEGAL_MOVES, START_FEN).build()));
            assertThrows(StockfishEngineException.class,
                    () -> stockfish.makeMoves(new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build()));
            assertThrows(StockfishEngineException.class, () -> stockfish.readLine(""));
            assertThrows(StockfishEngineException.class, () -> stockfish.readResponse(""));
        } catch (Exception e) {
            fail(e);
        }

    }

    @RepeatedTest(10)
    @Disabled("Not updated for removal of multi-instance client")
    void close() {
        try {
            assertEquals(1, getProcessNumber());
            stockfish.close();
            assertEquals(0, getProcessNumber());
            stockfish.close(); //assertThrows(StockfishEngineException.class, () -> stockfish.close()); // todo
        } catch (Exception e) {
            fail(e);
        }
    }

    private void setOutput(File tempFile) throws NoSuchFieldException, IOException, IllegalAccessException {
        Field output = stockfish.getClass().getSuperclass().getDeclaredField("output");
        output.setAccessible(true);
        output.set(stockfish, new BufferedWriter(new FileWriter(tempFile, false)));
        output.setAccessible(false);
    }

    private void setInput(File tempFile) throws NoSuchFieldException, FileNotFoundException, IllegalAccessException {
        Field input = stockfish.getClass().getSuperclass().getDeclaredField("input");
        input.setAccessible(true);
        input.set(stockfish, new BufferedReader(new InputStreamReader(new FileInputStream(tempFile))));
        input.setAccessible(false);
    }

    private File creteTempFile() throws IOException {
        return File.createTempFile("stockfish-", ".tmp");
    }


}
