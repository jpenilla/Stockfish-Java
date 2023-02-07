/* Copyright 2018 David Cai Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.niflheim.stockfish.engine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.exceptions.StockfishEngineException;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

/**
 * The StockfishClient for managing Stockfish processes,
 * as well as for interacting with the Stockfish API using {@link Query}.
 * <p>
 *
 * @author Niflheim
 * @see <a href="https://stockfishchess.org/">Stockfish website</a>
 * @see <a href="https://github.com/official-stockfish/Stockfish">Official Stochfish <b>Github</b> repository</a>
 * @since 1.0
 */
public class StockfishClient {

    private static final Logger LOGGER = LogManager.getLogger(StockfishClient.class);

    private final ExecutorService queryExecutor;
    private final Stockfish engine;


    /**
     * Private constructor for {@code StockfishClient} which is used by Builder to create a new instance
     *
     * @param path    path to folder with Stockfish core (default assets/engine)
     * @param options Stockfish launch options, see {@link xyz.niflheim.stockfish.engine.enums.Option} enum
     * @throws StockfishInitException throws if Stockfish process can not be initialized, starter or bind
     */
    private StockfishClient(Path path, Map<Option, String> options) throws StockfishInitException {
        this.queryExecutor = Executors.newSingleThreadExecutor();
        this.engine = new Stockfish(path, options);
    }

    /**
     * Method to execute UCI command as Query in Stockfish with callback.
     *
     * @param query query to execute in Stockfish
     * @return future
     * @see xyz.niflheim.stockfish.engine.enums.Query
     */
    public CompletableFuture<String> submit(final Query query) {
        return CompletableFuture.supplyAsync(() -> switch (query.getType()) {
            case Best_Move -> this.engine.getBestMove(query);
            case Make_Move -> this.engine.makeMove(query);
            case Legal_Moves -> this.engine.getLegalMoves(query);
            case Checkers -> this.engine.getCheckers(query);
        }, this.queryExecutor);
    }

    public CompletableFuture<Void> uciNewGame() {
        return CompletableFuture.runAsync(this.engine::uciNewGame, this.queryExecutor);
    }

    /**
     * This method close all Stockfish instances that were created, as well as close all
     * threads for processing responses. You must call this method when you close
     * your program to avoid uncontrolled memory leaks.
     * <p>
     * Exceptions are thrown only after trying to close all remaining threads
     *
     * @throws StockfishEngineException when at least one of the processes could not be closed.
     */
    public void close() throws StockfishEngineException {
        awaitTerminationAfterShutdown(queryExecutor);

        AtomicBoolean error = new AtomicBoolean(false);
        AtomicReference<Exception> ex = new AtomicReference<>();
        try {
            this.engine.close();
        } catch (IOException | StockfishEngineException e) {
            ex.set(e);
            error.compareAndSet(false, true);
            LOGGER.fatal("Can not stop Stockfish. Please, close it manually.", e);
        }
        if (error.get()) {
            throw new StockfishEngineException("Error while closing Stockfish threads", ex.get());
        }
    }

    private static void awaitTerminationAfterShutdown(final ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
        }
    }


    /**
     * Standard Builder pattern to create {@link StockfishClient} instance.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Builder_pattern">Wiki <b>Builder</b> pattern.</a>
     */
    public static class Builder {
        private final Map<Option, String> options = new HashMap<>();
        private Path path = null;

        /**
         * @param o     Stockfish launch options, see {@link xyz.niflheim.stockfish.engine.enums.Option} enum
         * @param value value of option
         * @return Builder to continue creating StockfishClient
         */
        public final Builder setOption(Option o, Object value) {
            options.put(o, value.toString());
            return this;
        }

        /**
         * @param path path to folder with Stockfish core (default assets/engine/)
         * @return Builder to continue creating StockfishClient
         */
        public final Builder setPath(Path path) {
            this.path = path;
            return this;
        }

        /**
         * @return ready StockfishClient with fields set
         * @throws StockfishInitException throws if Stockfish process can not be initialized, starter or bind
         */
        public final StockfishClient build() throws StockfishInitException {
            return new StockfishClient(path, options);
        }
    }
}
