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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.exceptions.StockfishEngineException;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

abstract class UCIEngine {
    final BufferedReader input;
    final BufferedWriter output;
    final Process process;
    final OutputStreamWriter directOut;

    UCIEngine(Path path, Map<Option, String> options) throws StockfishInitException {
        try {
            process = new ProcessBuilder()
                .command(path.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .start();
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            directOut = new OutputStreamWriter(process.getOutputStream());
            output = new BufferedWriter(directOut);

            waitForReady();
            for (Map.Entry<Option, String> option : options.entrySet()) {
                passOption(option.getKey(), option.getValue());
            }
        } catch (IOException | StockfishEngineException e) {
            throw new StockfishInitException("Unable to start and bind Stockfish process.", e);
        }
    }

    void waitForReady() {
        sendCommand("isready");
        readUntil("readyok");
    }

    void uciNewGame() {
        sendCommand("ucinewgame");
        waitForReady();
    }

    void sendCommand(String command) {
        try {
            if ("quit".equals(command)) {
                directOut.write(command + "\n");
                return;
            }
            output.write(command + "\n");
            output.flush();
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }

    String readLine(String expected) {
        final List<String> output = this.readUntil(expected);
        return output.get(output.size() - 1);
    }

    List<String> readUntil(String expected) {
        try {
            List<String> lines = new ArrayList<>();
            String line;
            boolean isPresent = false;
            while ((line = input.readLine()) != null) {
                lines.add(line);

                if (line.startsWith(expected)) {
                    isPresent = true;
                    break;
                }
            }
            if (isPresent) {
                return lines;
            } else {
                throw new StockfishEngineException("Can not find expected line: '" + expected + "' in output:\n  " + String.join("\n  ", lines));
            }
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }

    protected void passOption(Option option, String value) {
        sendCommand("setoption name " + option.optionString + " value " + value);
    }
}
