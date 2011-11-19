package com.cgdecker.luhnybin;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class SingleThreadedLuhnMasker implements LuhnMasker {

  @Override public void run(InputSupplier<? extends Reader> inSupplier, Writer out) {
    try {
      CharStreams.readLines(inSupplier, LuhnLineMasker.newWritingLineProcessor(out));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
