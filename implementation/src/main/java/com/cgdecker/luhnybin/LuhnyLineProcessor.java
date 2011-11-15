package com.cgdecker.luhnybin;

import com.google.common.base.CharMatcher;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnyLineProcessor implements LineProcessor<Void> {

  private final PrintWriter writer;

  public LuhnyLineProcessor(PrintWriter writer) {
    this.writer = writer;
  }

  public boolean processLine(String line) throws IOException {
    new LuhnyLineWriter(line, writer).process();
    return true;
  }

  public Void getResult() {
    return null;
  }

}
