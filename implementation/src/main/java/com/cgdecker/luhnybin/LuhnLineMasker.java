package com.cgdecker.luhnybin;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Handles processing single lines of input.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public final class LuhnLineMasker implements Callable<char[]> {

  /**
   * Masks any possible credit card sequences in the given line, returning the resulting line as a
   * char array.
   */
  public static char[] mask(String line) throws IOException {
    return new LuhnLineMasker(line).call();
  }

  private final char[] buffer;
  private final LuhnDigitBuffer digits = new LuhnDigitBuffer();

  public LuhnLineMasker(String line) {
    this.buffer = line.toCharArray();
  }

  /**
   * Processes the given line, writing the processed output to the given writer.
   */
  public char[] call() throws IOException {
    int pos = 0;
    while ((pos = nextDigit(pos)) < buffer.length) {
      pos = check(pos);
      if (pos == buffer.length)
        break;
    }
    return buffer;
  }

  /**
   * Returns the index of the next digit or the buffer length if the end of the buffer is reached.
   */
  private int nextDigit(int pos) {
    int i;
    for (i = pos; i < buffer.length; i++) {
      if (isDigit(buffer[i]))
        return i;
    }
    return i;
  }

  /**
   * Checks the characters of the string starting at {@code pos}, known to be a digit, masking
   * digits if needed. Returns the index of the next non-credit card character or the buffer length
   * if the end of the buffer is reached.
   */
  private int check(int pos) {
    char c;
    int i;
    for (i = pos; i < buffer.length; i++) {
      c = buffer[i];
      if (isDigit(c)) {
        digits.add(c, i);
        if (digits.length() >= 14)
          digits.mask(buffer);
      } else if (!isSeparator(c)) {
        digits.reset();
        break;
      }
    }

    return i;
  }

  private static boolean isSeparator(char c) {
    return c == ' ' || c == '-';
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }
}
