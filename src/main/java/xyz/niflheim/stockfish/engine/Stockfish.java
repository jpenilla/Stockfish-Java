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
import java.util.List;
import java.util.Map;
import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

class Stockfish extends UCIEngine {

    Stockfish(Path path, Map<Option, String> options) throws StockfishInitException {
        super(path, options);
    }

    String makeMove(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen() + " moves " + query.getMove());
        return getFen();
    }

    String getCheckers(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        waitForReady();
        sendCommand("d");

        return readLine("Checkers: ").substring(10);
    }

    String getBestMove(Query query) {
        if (query.getDifficulty() >= 0) {
            waitForReady();
            sendCommand("setoption name Skill Level value " + query.getDifficulty());
        }

        waitForReady();
        sendCommand("position fen " + query.getFen());

        StringBuilder command = new StringBuilder("go ");

        if (query.getDepth() >= 0) {
            command.append("depth ").append(query.getDepth()).append(" ");
        }

        if (query.getMovetime() >= 0) {
            command.append("movetime ").append(query.getMovetime());
        }

        waitForReady();
        sendCommand(command.toString());

        return readLine("bestmove").substring(9).split("\\s+")[0];
    }

    String getLegalMoves(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        waitForReady();
        sendCommand("go perft 1");

        StringBuilder legal = new StringBuilder();
        List<String> response = readResponse("Nodes");

        for (String line : response) {
            if (!line.contains("Nodes") && line.contains(":")) {
                legal.append(line.split(":")[0]).append(" ");
            }
        }

        return legal.toString();
    }

    void close() throws IOException {
        try {
            sendCommand("quit");
        } finally {
            if (process.isAlive()) {
                process.destroy();
            }
            //input.close();
            //output.close();
        }
    }

    private String getFen() {
        waitForReady();
        sendCommand("d");

        return readLine("Fen: ").substring(5);
    }
}
