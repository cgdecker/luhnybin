package com.cgdecker.luhnybin;

import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Handles processing a single line of input.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public final class LuhnLineMasker {

  /**
   * Creates a {@link LineProcessor} that writes processed lines to the given writer.
   */
  public static LineProcessor<Void> newWritingLineProcessor(Writer writer) {
    return new WritingProcessor(writer);
  }

  /**
   * Creates a {@link LineProcessor} that submits processing tasks to an executor and adds the resulting
   * {@link Future}s to a blocking queue for another thread to read.
   */
  public static LineProcessor<Void> newAsyncLineProcessor(ExecutorService executor,
      BlockingQueue<Future<char[]>> queue) {
    return new AsyncProcessor(executor, queue);
  }

  /**
   * Processes the given line, writing the processed output to the given writer.
   */
  public static char[] process(String line) throws IOException {
    return new LuhnLineMasker(line).run();
  }

  private final char[] buffer;
  private final int length;
  private final LuhnDigitBuffer digits = new LuhnDigitBuffer();

  private LuhnLineMasker(String line) {
    this.buffer = line.toCharArray();
    this.length = buffer.length;
  }

  private char[] run() throws IOException {
    int pos = 0;
    while ((pos = nextDigit(pos)) < length) {
      pos = check(pos);
      if (pos == length)
        break;
    }
    return buffer;
  }

  /**
   * Returns the index of the next digit or the buffer length if the end of the buffer is reached.
   */
  private int nextDigit(int pos) {
    int i;
    for (i = pos; i < length; i++) {
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
    for (i = pos; i < length; i++) {
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

  /**
   * {@link LineProcessor} implementation that writes filtered lines to the given writer.
   */
  private static final class WritingProcessor implements LineProcessor<Void> {

    private final Writer writer;

    public WritingProcessor(Writer writer) {
      this.writer = writer;
    }

    @Override public boolean processLine(String line) throws IOException {
      char[] result = process(line);
      writer.write(result);
      writer.write('\n');
      writer.flush();
      return true;
    }

    @Override public Void getResult() {
      return null;
    }
  }

  /**
   * A {@link LineProcessor} that submits processing tasks to an executor and adds the resulting
   * {@link Future}s to a blocking queue for another thread to read.
   */
  private static final class AsyncProcessor implements LineProcessor<Void> {

    private final ExecutorService executor;
    private final BlockingQueue<Future<char[]>> queue;

    public AsyncProcessor(ExecutorService executor, BlockingQueue<Future<char[]>> queue) {
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
      return process(line);
    }
  }
}
