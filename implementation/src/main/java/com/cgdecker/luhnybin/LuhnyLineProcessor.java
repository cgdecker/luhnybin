package com.cgdecker.luhnybin;

import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.Writer;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnyLineProcessor implements LineProcessor<Void> {

  private final Writer writer;

  public LuhnyLineProcessor(Writer writer) {
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
