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
package xyz.niflheim.stockfish.engine.enums;

/**
 * Variants of Stockfish. See the Stockfish documentation for details on what variant
 * will work best for your CPU.
 *
 * <p>As of writing, the BMI2 variant will be best for Intel 4th gen+ (ie i7 4770k and newer)
 * and AMD Zen 3+ (ie 5600X or newer). AVX2 will be best for AMD Zen pre-Zen 3.
 */
public enum Variant {
    DEFAULT,
    BMI2,
    AVX2,
    POPCNT,
    MODERN
}
