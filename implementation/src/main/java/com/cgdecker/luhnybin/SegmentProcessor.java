package com.cgdecker.luhnybin;

/**
 * Processes matching and unmatching segments of a stream.
 *
 * @author Colin Decker
 */
public interface SegmentProcessor {

  /**
   * Processes the given sequence of matching characters. {@code buffer} is guaranteed to start and
   * end with digits, contain at least 14 digits and not contain any non-digit characters other than
   * ' ' and '-'.
   */
  void processMatching(char[] buffer, int totalDigits);

  /**
   * Processes the given sequence of unmatching characters that does not need to be masked.
   */
  void processUnmatching(char[] buffer);

  /**
   * Called once all segments have been read and passed to the process methods and we've reached
   * end of file.
   */
  void done();
}
