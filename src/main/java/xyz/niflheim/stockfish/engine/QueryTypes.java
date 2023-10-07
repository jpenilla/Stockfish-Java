package xyz.niflheim.stockfish.engine;

import java.util.Set;

public final class QueryTypes {
    private QueryTypes() {
    }

    /**
     * Get best move for this FEN position.
     */
    public static final QueryType<String> BEST_MOVE = new QueryType<>();
    /**
     * Get a response move to the user's move.
     */
    public static final QueryType<String> MAKE_MOVES = new QueryType<>();
    /**
     * Get a list of all legal moves for this FEN position.
     */
    public static final QueryType<Set<String>> LEGAL_MOVES = new QueryType<>();
    public static final QueryType<String> CHECKERS = new QueryType<>();
}
