package com.cgdecker.luhnybin;

import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.io.Writer;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class LuhnyLineWriter {

  private static final CharMatcher SEPARATORS = CharMatcher.anyOf("- ").precomputed();

  private final String line;
  private final Writer writer;
  private final char[] buffer;

  public LuhnyLineWriter(String line, Writer writer) {
    this.line = line;
    this.writer = writer;
    this.buffer = line.toCharArray();
  }

  public void process() throws IOException {
    int pos = 0;
    while (pos != -1 && (pos = nextDigit(pos)) != -1) {
      pos = check(pos);
    }
    writer.write(buffer);
    writer.write('\n');
    writer.flush();
  }

  /**
   * Returns the index of the next digit or -1 if the end of line is reached.
   */
  private int nextDigit(int pos) {
    return CharMatcher.DIGIT.indexIn(line, pos);
  }

  /**
   * Checks the characters of the string starting at {@code pos}, known to be a digit, masking
   * digits if needed. Returns the index of the next non-credit card character or -1 if the end of
   * line is reached.
   */
  private int check(int pos) {
    int totalDigits = 0;
    int i = pos;
    int lastDigitIndex = i;
    char c;
    do {
      c = line.charAt(i);
      if (Character.isDigit(c)) {
        totalDigits++;
        lastDigitIndex = i;
      } else if (!SEPARATORS.matches(c)) {
        break;
      }
      i++;
    } while (i < line.length());

    int nextNonCcPos = i;

    if (totalDigits >= 14) {
      // we have a 14+ character substring with only digits, spaces and hyphens... check it
      check(pos, lastDigitIndex - pos + 1, totalDigits);
    }
    return nextNonCcPos == line.length() ? -1 : nextNonCcPos;
  }

  /**
   * Checks the given string, which has 14+ digits in it, determining if it has any credit card
   * numbers in it. If it does, the string is written to the result with their digits replaced
   * with Xs. If it does not, the string is written to the result as is.
   */
  private void check(int offset, int length, int totalDigits) {
    // use an array-based list that tracks the Luhn digit sums of the values in it
    LuhnyList digits = new LuhnyList(totalDigits);

    for (int i = offset; i < offset + length; i++) {
      char c = line.charAt(i);

      if (Character.isDigit(c)) {
        digits.addDigit(c, i - offset);

        if (digits.length() >= 14) {
          digits.mask(buffer, offset);
        }
      }
    }
  }
}
