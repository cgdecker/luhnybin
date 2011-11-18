package com.cgdecker.luhnybin;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * A multithreaded version... doesn't really do much for test speed since the test harness waits for
 * this process to output a line before it writes the next line for us to read. Would probably work
 * much better than the single-threaded version if it wasn't synchronous like that, but I imagine
 * that was intentional. Still, this somehow shaves a few microseconds per test off on long runs.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class Main {

  public static void main(String[] args) {
    boolean multiThreaded = args.length > 0 && "-m".equals(args[0]);
    LuhnMasker masker = multiThreaded ?
        new MultiThreadedLuhnMasker() :
        new SingleThreadedLuhnMasker();
    masker.run(standardInReaderSupplier(), standardOutWriter());
  }

  private static BufferedWriter standardOutWriter() {
    return new BufferedWriter(new OutputStreamWriter(System.out, Charsets.US_ASCII));
  }

  private static InputSupplier<InputStreamReader> standardInReaderSupplier() {
    return CharStreams.newReaderSupplier(
        new InputSupplier<InputStream>() {
          public InputStream getInput() throws IOException {
            return System.in;
          }
        }, Charsets.US_ASCII);
  }
}
