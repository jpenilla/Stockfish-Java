package xyz.niflheim.stockfish.engine;

/**
 * Query type
 *
 * @param <R> response type
 */
@SuppressWarnings("unused")
public record QueryType<R>() {
    public Query.Builder<R> builder(final String fen) {
        return new Query.Builder<>(this, fen);
    }
}
