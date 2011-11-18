package com.cgdecker.luhnybin;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Colin Decker
 */
public class MaskableStreams {

  public static void main(String[] args) {

  }

  public static void read(Reader reader, SegmentProcessor processor) throws IOException {
    new MatchingSequenceReader(reader, processor).readAll();
  }
}
