package com.cgdecker.luhnybin;

import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.io.Writer;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class LuhnyLineWriter {

  private static final CharMatcher CC_SEPARATORS = CharMatcher.anyOf("- ");
  private static final CharMatcher CC_CHARS = CharMatcher.DIGIT.or(CC_SEPARATORS);

  private final String line;
  private final Writer writer;
  private final char[] buffer;

  public LuhnyLineWriter(String line, Writer writer) {
    this.line = line;
    this.writer = writer;
    this.buffer = new char[line.length() + 1];
    buffer[buffer.length - 1] = '\n';
  }

  public void process() throws IOException {
    int pos = 0;
    while (pos != -1 && (pos = skip(pos)) != -1) {
      pos = check(pos);
    }
    writer.write(buffer);
    writer.flush();
  }

  /**
   * Finds the position of the next credit card character, appending all skipped characters to the
   * result. Returns the index of the next credit card character or -1 if the end of line is
   * reached.
   */
  private int skip(int pos) {
    char c;
    while (pos < line.length() && !CC_CHARS.matches((c = line.charAt(pos)))) {
      buffer[pos] = c;
      pos++;
    }
    return pos == line.length() ? -1 : pos;
  }

  /**
   * Checks the characters of the string starting at {@code pos}, a known credit card character,
   * appending the resulting characters (possibly with digits replaced by Xs) to the result.
   * Returns the index of the next non-credit card character or -1 if the end of line is reached.
   */
  private int check(int pos) {
    int totalDigits = 0;
    int i = pos;
    char c;
    do {
      c = line.charAt(i);
      buffer[i] = c;
      if (CharMatcher.DIGIT.matches(c)) {
        totalDigits++;
      } else if (!CC_SEPARATORS.matches(c)) {
        break;
      }
      i++;
    } while (i < line.length());

    int nextNonCcPos = i;

    if (totalDigits >= 14) {
      // we have a 14+ character substring with only digits, spaces and hyphens... check it
      check(pos, nextNonCcPos - pos, totalDigits);
    }
    return nextNonCcPos == line.length() ? -1 : nextNonCcPos;
  }

  /**
   * Checks the given string, which has 14+ digits in it, determining if it has any credit card
   * numbers in it. If it does, the string is written to the result with their digits replaced
   * with Xs. If it does not, the string is written to the result as is.
   */
  private void check(int offset, int length, int totalDigits) {
    // use a linked list that tracks the luhnyness of the values in it
    LuhnyList number = new LuhnyList(totalDigits);

    for (int i = offset; i < offset + length; i++) {
      char c = line.charAt(i);

      if (CharMatcher.DIGIT.matches(c)) {
        number.addDigit(c, i - offset);

        if (number.length() >= 14) {
          // don't need to check/mask any shorter lists ending at this index if we mask the whole
          // thing
          boolean masked = false;
          for (LuhnyList numberToCheck = number; !masked && numberToCheck.length() >= 14;
               numberToCheck = numberToCheck.dropFirst()) {
            masked = numberToCheck.mask(buffer, offset);
          }
        }
      }
    }

    // writer.append(buffer);
  }
}
