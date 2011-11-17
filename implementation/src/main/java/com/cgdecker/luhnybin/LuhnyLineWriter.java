package com.cgdecker.luhnybin;

import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.Writer;

/**
 * Handles processing a single line of input.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public final class LuhnyLineWriter {

  /**
   * Creates a {@link LineProcessor} implementation that writes filtered lines to the given writer.
   */
  public static LineProcessor<Void> newLineProcessor(Writer writer) {
    return new Processor(writer);
  }

  /**
   * Processes the given line, writing the processed output to the given writer.
   */
  public static void process(String line, Writer writer) throws IOException {
    new LuhnyLineWriter(line, writer).run();
  }

  private final Writer writer;
  private final char[] buffer;

  private LuhnyLineWriter(String line, Writer writer) {
    this.writer = writer;
    this.buffer = line.toCharArray();
  }

  private void run() throws IOException {
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
    for (int i = pos; i < buffer.length; i++) {
      if (isDigit(buffer[i]))
        return i;
    }
    return -1;
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
      check(pos, lastDigitIndex - pos + 1, totalDigits);
    }
    return nextNonCcPos == buffer.length ? -1 : nextNonCcPos;
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
      char c = buffer[i];

      if (isDigit(c)) {
        digits.add(c, i - offset);

        if (digits.length() >= 14) {
          digits.mask(buffer, offset);
        }
      }
    }
  }

  private static boolean isSeparator(char c) {
    return c == ' ' || c == '-';
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  /**
   * {@link LineProcessor} implementation that writes filtered lines to the given writer.
   */
  private static final class Processor implements LineProcessor<Void> {
    private final Writer writer;

    public Processor(Writer writer) {
      this.writer = writer;
    }

    public boolean processLine(String line) throws IOException {
      LuhnyLineWriter.process(line, writer);
      return true;
    }

    public Void getResult() {
      return null;
    }
  }
}
