package com.cgdecker.luhnybin;

import com.google.common.io.InputSupplier;

import java.io.Reader;
import java.io.Writer;

/**
 * Reads input lines and writes them to to output with possible credit card numbers masked.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public interface LuhnMasker {

  /**
   * Reads from the {@link Reader} supplied by the given supplier and writes masked lines to the
   * given {@link Writer}.
   */
  void run(InputSupplier<? extends Reader> inSupplier, Writer out);
}
