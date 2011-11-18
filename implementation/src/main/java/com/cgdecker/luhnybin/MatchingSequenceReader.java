package com.cgdecker.luhnybin;

import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Colin Decker
 */
final class MatchingSequenceReader {

  private static final CharMatcher DIGITS = CharMatcher.inRange('0', '9').precomputed();
  private static final CharMatcher CC_CHARS = DIGITS.or(CharMatcher.anyOf(" -")).precomputed();

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final Reader reader;
  private final SegmentProcessor processor;
  private final char[] buffer = new char[8192];

  MatchingSequenceReader(Reader reader, SegmentProcessor processor) {
    this.reader = reader;
    this.processor = processor;
  }

  public final void readAll() throws IOException {
    int read;
    State state = new UnmatchingState(0);
    while ((read = reader.read(buffer)) != -1) {
      for (int i = 0; i < read; i++) {
        char c = buffer[i];
        state = state.handle(c, i);
      }
      state = state.handleBufferEnd(read);
    }
    state.finish();
  }

  /**
   * Abstraction for the current state.
   */
  private abstract class State {

    protected final int start;

    public State(int start) {
      this.start = start;
    }

    /**
     * Handles the reading of the given char from the given position in the buffer, returning a the
     * resulting state.
     */
    abstract State handle(char c, int pos);

    /**
     * Handles the end of the range that was read in to the buffer.
     */
    abstract State handleBufferEnd(int pos);

    /**
     * Writes any buffered characters out.
     */
    void finish() {}
  }

  /**
   * Currently in a segment of matching characters.
   */
  private class MatchingState extends State {

    private int totalDigits;

    private MatchingState(int start, char first) {
      super(start);

      if (DIGITS.matches(first)) {
        totalDigits = 1;
      }
    }

    @Override State handle(char c, int pos) {
      if (DIGITS.matches(c)) {
        totalDigits++;
      } else if (!CC_CHARS.matches(c)) {
        if (totalDigits >= 14) {
          processor.processMatching(copySegment(buffer, start, pos), totalDigits);
          return new UnmatchingState(pos);
        } else {
          return new UnmatchingState(start);
        }
      }
      return this;
    }

    @Override State handleBufferEnd(int pos) {
      return new WrappedMatchingState(copySegment(buffer, start, pos), totalDigits);
    }
  }

  /**
   * Currently in a segment of unmatching characters.
   */
  private class UnmatchingState extends State {

    private UnmatchingState(int start) {
      super(start);
    }

    @Override State handle(char c, int pos) {
      if (DIGITS.matches(c)) {
        processor.processUnmatching(copySegment(buffer, start, pos));
        return new MatchingState(pos, c);
      }
      return this;
    }

    @Override State handleBufferEnd(int pos) {
      processor.processUnmatching(copySegment(buffer, start, pos));
      return new UnmatchingState(0);
    }
  }

  /**
   * Currently in a segment of matching characters where we have a buffer of matching characters from
   * a previous iteration.
   */
  private class WrappedMatchingState extends State {

    private final char[] previousSegment;
    private int totalDigits;

    public WrappedMatchingState(char[] previousSegment, int totalDigits) {
      super(0);
      this.previousSegment = previousSegment;
      this.totalDigits = totalDigits;
    }

    @Override State handle(char c, int pos) {
      if (DIGITS.matches(c)) {
        totalDigits++;
      } else if (!CC_CHARS.matches(c)) {
        processor.processMatching(concatBuffers(previousSegment, buffer, start, pos), totalDigits);
        return new UnmatchingState(pos);
      }
      return this;
    }

    @Override State handleBufferEnd(int pos) {
      return new WrappedMatchingState(concatBuffers(previousSegment, buffer, start, pos),
          totalDigits);
    }

    @Override void finish() {
      processor.processMatching(previousSegment, totalDigits);
    }
  }

  private static char[] copySegment(char[] buffer, int start, int end) {
    int length = end - start;
    char[] result = new char[length];
    System.arraycopy(buffer, start, result, 0, length);
    return result;
  }

  private static char[] concatBuffers(char[] a, char[] b, int from, int to) {
    int length = to - from;
    char[] result = new char[a.length + length];
    System.arraycopy(a, 0, result, 0, a.length);
    if (length != 0)
      System.arraycopy(b, from, result, a.length, length);
    return result;
  }
}
