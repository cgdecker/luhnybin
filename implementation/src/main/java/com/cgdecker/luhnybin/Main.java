package com.cgdecker.luhnybin;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * General approach: read lines from stdin. For each line, split on all characters that may not be
 * part of a credit card number. For each remaining token, filter out all tokens with fewer than 14
 * characters. Then begin checking each remaining token. For a token, filter out or ignore all non-
 * digit characters. Proceed left to right through the digits. For each digit, place the original
 * digit in one list and the doubled digit in another. Alternate which list gets which. Keep a
 * running total and, when each list is at or above the minimum number of digits, check
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class Main {

  public static void main(String[] args) {
    InputSupplier<InputStreamReader> standardInSupplier = CharStreams.newReaderSupplier(
        new InputSupplier<InputStream>() {
          public InputStream getInput() throws IOException {
            return new BufferedInputStream(System.in);
          }
        }, Charset.defaultCharset());

    Writer out = new OutputStreamWriter(System.out, Charset.defaultCharset());
    try {
      CharStreams.readLines(standardInSupplier, new LuhnyLineProcessor(out));
    } catch (IOException e) {
      System.exit(1);
    }
  }

}
