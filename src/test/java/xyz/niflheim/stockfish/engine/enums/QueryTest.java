package xyz.niflheim.stockfish.engine.enums;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.niflheim.stockfish.engine.Query;
import xyz.niflheim.stockfish.engine.QueryTypes;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.niflheim.stockfish.util.StringUtil.START_FEN;

class QueryTest {

    private Query<String> query;

    @BeforeEach
    void setUp() {
        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getType() {
        Query<?> query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
        assertEquals(QueryTypes.MAKE_MOVES, query.getType());

        query = new Query.Builder<>(QueryTypes.LEGAL_MOVES, START_FEN).build();
        assertEquals(QueryTypes.LEGAL_MOVES, query.getType());

        query = new Query.Builder<>(QueryTypes.BEST_MOVE, START_FEN).build();
        assertEquals(QueryTypes.BEST_MOVE, query.getType());

        query = new Query.Builder<>(QueryTypes.CHECKERS, START_FEN).build();
        assertEquals(QueryTypes.CHECKERS, query.getType());

        assertThrows(IllegalStateException.class, () -> new Query.Builder<>(null, START_FEN).build());
    }

    @Test
    void getFen() {
        Query<String> query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
        assertEquals(START_FEN, query.getFen());

        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b KQkq - 0 1").build();
        assertEquals("8/8/8/8/8/8/8/8 b KQkq - 0 1", query.getFen());

        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 w kkkk - 10 10").build();
        assertEquals("8/8/8/8/8/8/8/8 w kkkk - 10 10", query.getFen());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8//8/8/8 w kkkk - 10 10").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 t kkkk - 10 10").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b tkkk - 10 10").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b kkkkk - 10 10").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b kkkk aa 10 10").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b kkkk a4a5 aa 10").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "8/8/8/8/8/8/8/8 b kkkk a4a5 10 aa").build());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, "hello world").build());

        assertThrows(IllegalStateException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, null).build());
    }

    @Test
    void getMove() {
        Query<String> query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
        assertNull(query.getMoves());
        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a2a4").build();
        assertEquals("a2a4", query.getMoves());
        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("h1a8").build();
        assertEquals("h1a8", query.getMoves());

        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves(null).build());
        // TODO
        /*
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMove("").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a4").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("A4B4").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("aaaa").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a4a9").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a4v5").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a4a6a").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMoves("a4a4a4").build());
         */

    }

    @Test
    void getDifficulty() {
        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
        assertEquals(-1, query.getDifficulty());


        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setDifficulty(10).build();
        assertEquals(10, query.getDifficulty());


        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setDifficulty(-10).build();
        assertEquals(-10, query.getDifficulty());
    }

    @Test
    void getDepth() {
        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
        assertEquals(-1, query.getDepth());


        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setDepth(10).build();
        assertEquals(10, query.getDepth());


        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setDepth(-10).build();
        assertEquals(-10, query.getDepth());
    }

    @Test
    void getMovetime() {
        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).build();
        assertEquals(-1, query.getMovetime());


        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMovetime(10).build();
        assertEquals(10, query.getMovetime());


        query = new Query.Builder<>(QueryTypes.MAKE_MOVES, START_FEN).setMovetime(-10).build();
        assertEquals(-10, query.getMovetime());
    }
}
