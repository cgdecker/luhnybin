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
    for (int i = pos; i < buffer.length; i++) {
      if (isDigit(buffer[i]))
        return i;
    }
    return buffer.length;
  }

  /**
   * Checks the characters of the string starting at {@code pos}, known to be a digit, masking
   * digits if needed. Returns the index of the next non-credit card character or the buffer length
   * if the end of the buffer is reached.
   */
  private int check(int pos) {
    int totalDigits = 0;
    int i = pos;
    int lastDigitIndex = i;
    char c;
    do {
      c = buffer[i];
      if (isDigit(c)) {
        totalDigits++;
        lastDigitIndex = i;
      } else if (!isSeparator(c)) {
        break;
      }
      i++;
    } while (i < buffer.length);

    int nextNonCcPos = i;

    if (totalDigits >= 14) {
      // we have a 14+ character substring with only digits, spaces and hyphens... check it
      mask(pos, lastDigitIndex - pos + 1, totalDigits);
    }
    return nextNonCcPos;
  }

  /**
   * Checks the given range (containing 14+ digits) in the buffer, masking any possible credit card numbers
   * in it.
   */
  private void mask(int offset, int length, int totalDigits) {
    LuhnDigitBuffer digits = new LuhnDigitBuffer(totalDigits);

    for (int i = offset; i < offset + length; i++) {
      char c = buffer[i];

      if (isDigit(c)) {
        digits.add(c, i);
        digits.mask(buffer);
      }
    }
  }

  private static boolean isSeparator(char c) {
    return c == ' ' || c == '-';
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }
}
