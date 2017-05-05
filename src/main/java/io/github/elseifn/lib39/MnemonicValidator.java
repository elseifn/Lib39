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

import io.github.elseifn.lib39.Validation.InvalidChecksumException;
import io.github.elseifn.lib39.Validation.InvalidWordCountException;
import io.github.elseifn.lib39.Validation.WordNotFoundException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static io.github.elseifn.lib39.MnemonicGenerator.firstByteOfSha256;

public final class MnemonicValidator {
    private final WordAndIndex[] words;
    final CharSequenceSplitter charSequenceSplitter;

    private MnemonicValidator(final WordList wordList) {
        words = new WordAndIndex[1 << 11];
        for (int i = 0; i < 1 << 11; i++) {
            final String word = wordList.getWord(i);
            words[i] = new WordAndIndex(i, word);
        }
        charSequenceSplitter = new CharSequenceSplitter(wordList.getSpace());
        Arrays.sort(words, wordListSortOrder);
    }

    public static MnemonicValidator ofWordList(WordList wordList) {
        return new MnemonicValidator(wordList);
    }

    public void validate(final CharSequence mnemonic) throws
            InvalidChecksumException,
            InvalidWordCountException,
            WordNotFoundException {
        final int[] wordIndexes = findWordIndexes(mnemonic);
        final int ms = wordIndexes.length;

        final int entPlusCs = ms * 11;
        final int ent = (entPlusCs * 32) / 33;
        final int cs = ent / 32;
        if (entPlusCs != ent + cs)
            throw new InvalidWordCountException();
        final byte[] entropyWithChecksum = new byte[(entPlusCs + 7) / 8];

        wordIndexesToEntropyWithCheckSum(wordIndexes, entropyWithChecksum);
        Arrays.fill(wordIndexes, 0);

        final byte[] entropy = Arrays.copyOf(entropyWithChecksum, entropyWithChecksum.length - 1);
        final byte lastByte = entropyWithChecksum[entropyWithChecksum.length - 1];
        Arrays.fill(entropyWithChecksum, (byte) 0);

        final byte sha = firstByteOfSha256(entropy);

        final byte mask = maskOfFirstNBits(cs);

        if (((sha ^ lastByte) & mask) != 0)
            throw new InvalidChecksumException();
    }

    private int[] findWordIndexes(final CharSequence mnemonic) throws WordNotFoundException {
        final List<CharSequence> split = charSequenceSplitter.split(mnemonic);
        final int ms = split.size();
        final int[] result = new int[ms];
        for (int i = 0; i < ms; i++) {
            result[i] = findWordIndex(split.get(i));
        }
        return result;
    }

    private int findWordIndex(final CharSequence buffer) throws WordNotFoundException {
        final WordAndIndex key = new WordAndIndex(-1, buffer);
        final int index = Arrays.binarySearch(words, key, wordListSortOrder);
        if (index < 0) {
            final int insertionPoint = -index - 1;
            int suggestion = insertionPoint == 0 ? insertionPoint : insertionPoint - 1;
            if (suggestion + 1 == words.length) suggestion--;
            throw new WordNotFoundException(buffer, words[suggestion].word, words[suggestion + 1].word);

        }
        return words[index].index;
    }

    private void wordIndexesToEntropyWithCheckSum(int[] wordIndexes, byte[] entropyWithChecksum) {
        for (int i = 0, bi = 0; i < wordIndexes.length; i++, bi += 11) {
            ByteUtils.writeNext11(entropyWithChecksum, wordIndexes[i], bi);
        }
    }

    private byte maskOfFirstNBits(int n) {
        return (byte) ~((1 << (8 - n)) - 1);
    }

    static final Comparator<WordAndIndex> wordListSortOrder = new Comparator<WordAndIndex>() {
        @Override
        public int compare(WordAndIndex o1, WordAndIndex o2) {
            return CharSequenceComparators.ALPHABETICAL.compare(o1.word, o2.word);
        }
    };

    private class WordAndIndex {
        final CharSequence word;
        final int index;

        WordAndIndex(int i, CharSequence word) {
            this.word = word;
            index = i;
        }
    }
}
