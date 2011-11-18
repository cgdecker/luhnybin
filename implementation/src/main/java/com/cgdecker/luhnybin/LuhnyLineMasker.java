package com.cgdecker.luhnybin;

import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.Writer;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Handles processing a single line of input.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public final class LuhnyLineMasker {

  /**
   * Creates a {@link LineProcessor} implementation that submits line masking tasks to the given
   * executor and adds the resulting futures to the end of the given queue.
   */
  public static LineProcessor<Void> newLineProcessor(ExecutorService executor,
      BlockingQueue<Future<char[]>> queue) {
    return new Processor(executor, queue);
  }

  /**
   * Processes the given line, writing the processed output to the given writer.
   */
  public static char[] process(String line) throws IOException {
    return new LuhnyLineMasker(line).run();
  }

  private final char[] buffer;

  private LuhnyLineMasker(String line) {
    this.buffer = line.toCharArray();
  }

  private char[] run() throws IOException {
    int pos = 0;
    while (pos != -1 && (pos = nextDigit(pos)) != -1) {
      pos = check(pos);
    }
    return buffer;
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
    private final ExecutorService executor;
    private final BlockingQueue<Future<char[]>> queue;

    public Processor(ExecutorService executor, BlockingQueue<Future<char[]>> queue) {
      this.executor = executor;
      this.queue = queue;
    }

    public boolean processLine(String line) throws IOException {
      try {
        queue.put(executor.submit(new ProcessLineTask(line)));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      return true;
    }

    public Void getResult() {
      return null;
    }
  }

  private static final class ProcessLineTask implements Callable<char[]> {

    private final String line;

    public ProcessLineTask(String line) {
      this.line = line;
    }

    @Override public char[] call() throws Exception {
      return LuhnyLineMasker.process(line);
    }
  }
}
