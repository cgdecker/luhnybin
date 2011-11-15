package com.cgdecker.luhnybin;

import com.google.common.base.CharMatcher;

import java.io.PrintWriter;
import java.nio.CharBuffer;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class LuhnyLineWriter {

  private static final CharMatcher CC_SEPARATORS = CharMatcher.anyOf("- ");
  private static final CharMatcher CC_CHARS = CharMatcher.DIGIT.or(CC_SEPARATORS);

  private final String line;
  private final PrintWriter writer;

  public LuhnyLineWriter(String line, PrintWriter writer) {
    this.line = line;
    this.writer = writer;
  }

  public void process() {
    int pos = 0;
    while (pos != -1 && (pos = skip(pos)) != -1) {
      pos = check(pos);
    }
    writer.write('\n');
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
      writer.append(c);
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
    // find the index of the next non-CC char first, to save us work if there aren't enough chars
    // to make a CC number
    int nextNonCcPos = pos + 1;
    int totalDigits = CharMatcher.DIGIT.matches(line.charAt(pos)) ? 1 : 0;
    while (nextNonCcPos < line.length()) {
      char c = line.charAt(nextNonCcPos++);
      if (CharMatcher.DIGIT.matches(c)) {
        totalDigits++;
      } else if (!CC_SEPARATORS.matches(c)) {
        break;
      }
    }

    if (totalDigits < 14) { // can't be CC number, too few digits
      if (nextNonCcPos == line.length()) // EOL
        writer.append(line.substring(pos));
      else
        writer.append(line.substring(pos, nextNonCcPos));
    } else {
      // we have a 14+ character substring with only digits, spaces and hyphens... check it
      check(line.substring(pos, nextNonCcPos));
    }
    return nextNonCcPos == line.length() ? -1 : nextNonCcPos;
  }

  /**
   * Checks the given string, which has 14+ digits in it, determining if it has any credit card
   * numbers in it. If it does, the string is written to the result with their digits replaced
   * with Xs. If it does not, the string is written to the result as is.
   */
  private void check(String possibleCc) {
    // use a linked list that tracks the luhnyness of the values in it
    LuhnyList number = new LuhnyList();

    CharBuffer buffer = CharBuffer.allocate(possibleCc.length());

    for (int i = 0; i < possibleCc.length(); i++) {
      char c = possibleCc.charAt(i);

      // go ahead and set the original value... only need to replace it if there's a match
      buffer.put(i, c);

      if (CharMatcher.DIGIT.matches(c)) {
        number.addDigit(c, i);

        // don't need to check/mask any shorter lists ending at this index if we mask the whole thing
        boolean masked = number.mask(buffer);

        if (!masked && number.length() > 14) {
          for (LuhnyList shorterNumber = number.dropFirst();
               !masked && shorterNumber.length() >= 14;
               shorterNumber = shorterNumber.dropFirst()) {
            masked = shorterNumber.mask(buffer);
          }
        }
      }
    }

    writer.append(buffer);
  }
}
