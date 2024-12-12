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

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Alexandre Haddad-Delaveau
 */
public class TextCompressor {

    static int EOF = 0x100;
    static int START = EOF + 1;

    static int CURRENT_BITS = 9; // # of bits per code / char (9 MINIMUM)
    static int MAX_CODE = (int) (Math.pow(2, CURRENT_BITS)) - 1;

    private static void writeCode(String value, TST codes, boolean lastCode) {
        if (value.length() == 1) {
            BinaryStdOut.write((int) value.charAt(0), CURRENT_BITS);
        } else {
            BinaryStdOut.write(codes.lookup(value), CURRENT_BITS);
        }

        // Write EOF value if this is the last code
        if (lastCode) {
            BinaryStdOut.write(EOF, CURRENT_BITS);
            BinaryStdOut.close();
        }
    }

    private static void increaseSize() {
        CURRENT_BITS++;
        MAX_CODE = (int) (Math.pow(2, CURRENT_BITS));
    }

    private static void compress() {
        // Store codes in TST
        TST codes = new TST();
        int currentCode = START;

        // Read input file
        String chars = String.valueOf(BinaryStdIn.readChar());
        while (!BinaryStdIn.isEmpty()) {

            // Read chars until no existing match is found in codes
            while (codes.lookup(chars) != TST.EMPTY || chars.length() == 1) {
                try {
                    chars += BinaryStdIn.readChar(8);
                } catch (NoSuchElementException exception) {
                    // If we've reached the end of the file, write the value & exit
                    writeCode(chars, codes, true);
                    System.exit(0);
                }
            }

            // Substring the written value (no lookahead character)
            String writtenValue = chars.substring(0, chars.length() - 1);

            // Create new code with characters read, as long as we haven't reached the max code
            if (currentCode >= MAX_CODE) {
                increaseSize();
            }

            codes.insert(chars, currentCode);
            currentCode++;

            // Write value to output
            writeCode(writtenValue, codes, BinaryStdIn.isEmpty());

            // Reset chars string
            chars = chars.substring(chars.length() - 1);
        }
    }

    private static void expand() {
        // Store codes in a Map
        String[] codes = new String[Integer.MAX_VALUE/2];
        int currentCode = START;

        // Read codes
        int code = BinaryStdIn.readInt(CURRENT_BITS);
        while (!BinaryStdIn.isEmpty()) {
            // Exit early if EOF is reached
            if (code == EOF) {
                break;
            }

            // Get string value associated with code
            String codeValue = code < START ? String.valueOf((char) code) : codes[code];

            // Increase bit count if necessary
            if (currentCode == MAX_CODE - 1) {
                increaseSize();
            }

            // Read lookahead code
            int lookaheadCode = BinaryStdIn.readInt(CURRENT_BITS);

            // Write new code
            if (currentCode < MAX_CODE) {
                if (lookaheadCode == currentCode) {
                    // Edge case
                    String newCodeValue = codeValue + codeValue.charAt(0);
                    codes[currentCode] = newCodeValue;
                } else {
                    String lookaheadCodeValue = codes[lookaheadCode] != null ? codes[lookaheadCode] : String.valueOf((char) lookaheadCode);
                    String newCodeValue = codeValue + lookaheadCodeValue.charAt(0);
                    codes[currentCode] = newCodeValue;
                }
                currentCode++;
            }

            // Write value
            BinaryStdOut.write(codeValue);

            // Update current code
            code = lookaheadCode;
        }

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
