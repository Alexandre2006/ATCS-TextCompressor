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

import java.util.NoSuchElementException;

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Alexandre Haddad-Delaveau
 */
public class TextCompressor {

    static int EOF = 0x80;
    static int BITS = 8; // # of bits per code / char (8 MINIMUM)
    static int MAX_CODE = (int) (Math.pow(2, BITS));

    private static void writeCode(String value, TST codes, boolean lastCode) {
        if (value.length() == 1) {
            BinaryStdOut.write(value, BITS);
        } else {
            BinaryStdOut.write(codes.lookup(value), BITS);
        }

        // Write EOF value if this is the last code
        if (lastCode) {
            BinaryStdOut.write(EOF, BITS);
        }
    }

    private static void compress() {
        // Store codes in TST
        TST codes = new TST();
        int currentCode = 0x81;

        // Read input file
        String chars = String.valueOf(BinaryStdIn.readChar());
        while (!BinaryStdIn.isEmpty()) {

            // Read chars until no existing match is found in codes
            while (codes.lookup(chars) != TST.EMPTY) {
                try {
                    chars += BinaryStdIn.readChar();
                } catch (NoSuchElementException exception) {
                    // If we've reached the end of the file, write the value & exit
                    writeCode(chars, codes, true);
                    System.exit(0);
                }
            }

            // Substring the written value (no lookahead character)
            String writtenValue = chars.substring(0, chars.length() - 1);

            // Create new code with characters read, as long as we haven't reached the max code
            if (currentCode < MAX_CODE) {
                codes.insert(chars, currentCode);
                currentCode++;
            }

            // Write value to output
            writeCode(writtenValue, codes, BinaryStdIn.isEmpty());

            // Reset chars string
            chars = chars.substring(chars.length() - 1);
        }

        BinaryStdOut.close();
    }

    private static void expand() {
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
