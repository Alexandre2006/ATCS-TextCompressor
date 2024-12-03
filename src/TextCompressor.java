/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, YOUR NAME HERE
 */
public class TextCompressor {

    private static void compress() {

        LinkedHashSet<String> wordsSet = new LinkedHashSet<>();
        ArrayList<String> words = new ArrayList<>();
        String currentWord = "";

        while (!BinaryStdIn.isEmpty()) {
            char currentChar = BinaryStdIn.readChar();
            if (currentChar == ' ') {
                wordsSet.add(currentWord);
                words.add(currentWord);
                currentWord = "";
            } else {
                currentWord += currentChar;
            }
        }

        // Add last word
        if (!currentWord.isEmpty()) {
            wordsSet.add(currentWord);
            words.add(currentWord);
        }

        // Write number of words
        BinaryStdOut.write(wordsSet.size());

        // Write word set with separator
        for (String word : wordsSet) {
            BinaryStdOut.write(word);
            BinaryStdOut.write((char) 0);
        }

        // Write words
        ArrayList<String> wordSetList = new ArrayList<>(wordsSet);
        for (String word : words) {
            int index = wordSetList.indexOf(word);
            BinaryStdOut.write(index);
        }

        BinaryStdOut.close();
    }

    private static void expand() {

        // Read # of words
        ArrayList<String> wordsSet = new ArrayList<>();
        int numWords = BinaryStdIn.readInt();

        // Read word set in
        String currentWord = "";
        int currentWordCount = 0;
        while (currentWordCount < numWords) {
            char nextChar = BinaryStdIn.readChar();

            if (nextChar == (char) 0) {
                wordsSet.add(currentWord);
                currentWord = "";
                currentWordCount++;
            } else {
                currentWord += nextChar;
            }
        }

        // Read words
        while (!BinaryStdIn.isEmpty()) {
            int index = BinaryStdIn.readInt();
            BinaryStdOut.write(wordsSet.get(index));

            // Add space if there is a next word
            if (!BinaryStdIn.isEmpty()) {
                BinaryStdOut.write(" ");
            }
        }

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
