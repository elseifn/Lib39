/*
 *  BIP39 library, a Java implementation of BIP39
 *  Copyright (C) 2017 Tongjian Cui, elseifn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Original source: https://github.com/elseifn/Lib39
 *  You can contact the authors via github issues.
 */

package io.github.elseifn.lib39;

import io.github.elseifn.lib39.testjson.EnglishJson;
import io.github.elseifn.lib39.testjson.TestVector;
import io.github.elseifn.lib39.testjson.TestVectorJson;
import io.github.elseifn.lib39.wordlists.English;
import io.github.elseifn.lib39.wordlists.French;
import io.github.elseifn.lib39.wordlists.Japanese;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class MnemonicGenerationTests {

    private static String createMnemonic(String f, WordList wordList) {
        final StringBuilder sb = new StringBuilder();
        new MnemonicGenerator(wordList)
                .createMnemonic(f, sb::append);
        return sb.toString();
    }

    private static String createMnemonic(byte[] f, WordList wordList) {
        final StringBuilder sb = new StringBuilder();
        new MnemonicGenerator(wordList)
                .createMnemonic(f, sb::append);
        return sb.toString();
    }

    @Test(expected = RuntimeException.class)
    public void tooSmallEntropy() throws Exception {
        createMnemonic(repeatString(30, "f"), English.INSTANCE);
    }

    @Test(expected = RuntimeException.class)
    public void tooSmallEntropyBytes() throws Exception {
        createMnemonic(new byte[15], English.INSTANCE);
    }

    @Test(expected = RuntimeException.class)
    public void tooLargeEntropy() throws Exception {
        createMnemonic(repeatString(66, "f"), English.INSTANCE);
    }

    @Test(expected = RuntimeException.class)
    public void tooLargeEntropyBytes() throws Exception {
        createMnemonic(new byte[33], English.INSTANCE);
    }

    @Test(expected = RuntimeException.class)
    public void nonMultipleOf32() throws Exception {
        createMnemonic(repeatString(34, "f"), English.INSTANCE);
    }

    @Test(expected = RuntimeException.class)
    public void notHexPairs() throws Exception {
        createMnemonic(repeatString(33, "f"), English.INSTANCE);
    }

    @Test
    public void sevenFRepeated() throws Exception {
        assertEquals("legal winner thank year wave sausage worth useful legal winner thank year wave sausage worth useful legal will",
                createMnemonic("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", English.INSTANCE)
        );
    }

    @Test
    public void eightZeroRepeated() throws Exception {
        assertEquals("letter advice cage absurd amount doctor acoustic avoid letter advice cage above",
                createMnemonic("80808080808080808080808080808080", English.INSTANCE)
        );
    }

    @Test
    public void all_english_test_vectors() throws Exception {
        final EnglishJson data = EnglishJson.load();
        for (final String[] testCase : data.english) {
            assertEquals(testCase[1], createMnemonic(testCase[0], English.INSTANCE));
        }
    }

    @Test
    public void all_japanese_test_vectors() throws Exception {
        final TestVectorJson data = TestVectorJson.loadJapanese();
        for (final TestVector testVector : data.vectors) {
            assertEquals(testVector.mnemonic, createMnemonic(testVector.entropy, Japanese.INSTANCE));
        }
    }

    @Test
    public void all_french_test_vectors() throws Exception {
        final TestVectorJson data = TestVectorJson.loadFrench();
        for (final TestVector testVector : data.vectors) {
            assertEquals(testVector.mnemonic, createMnemonic(testVector.entropy, French.INSTANCE));
        }
    }

    @Test
    public void upper_and_lower_case_hex_handled_the_same() throws Exception {
        final String hex = "0123456789abcdef0123456789abcdef";
        assertEquals(createMnemonic(hex, English.INSTANCE),
                createMnemonic(hex.toUpperCase(), English.INSTANCE));
    }

    @Test(expected = RuntimeException.class)
    public void bad_hex_throws_g() throws Exception {
        final String hex = "0123456789abcdef0123456789abcdeg";
        try {
            createMnemonic(hex, English.INSTANCE);
        } catch (final RuntimeException e) {
            assertEquals("Invalid hex char 'g'", e.getMessage());
            throw e;
        }
    }

    @Test(expected = RuntimeException.class)
    public void bad_hex_throws_Z() throws Exception {
        final String hex = "0123456789abcdef0123456789abcdeZ";
        try {
            createMnemonic(hex, English.INSTANCE);
        } catch (final RuntimeException e) {
            assertEquals("Invalid hex char 'Z'", e.getMessage());
            throw e;
        }
    }

    private static String repeatString(int n, String repeat) {
        return new String(new char[n]).replace("\0", repeat);
    }
}
