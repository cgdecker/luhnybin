package com.squareup.luhnybin;

import com.cgdecker.luhnybin.LuhnMaskers;
import com.google.common.base.Stopwatch;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.fail;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class ChallengeTest {

  @Test public void runTests() throws IOException {
    final LuhnyBinTests tests = new LuhnyBinTests();

    final PipedOutputStream testOut = new PipedOutputStream();
    final PipedInputStream inFromTest = new PipedInputStream(testOut);

    ExecutorService executor = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder().setDaemon(true).build());

    executor.execute(new Runnable() {
      @Override public void run() {
        try {
          tests.writeTo(testOut);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });

    final PipedOutputStream implOut = new PipedOutputStream();
    final PipedInputStream inFromImpl = new PipedInputStream(implOut);

    executor.execute(new Runnable() {
      @Override public void run() {
        InputSupplier<InputStreamReader> inFromTestSupplier = CharStreams.newReaderSupplier(
        new InputSupplier<InputStream>() {
          public InputStream getInput() throws IOException {
            return inFromTest;
          }
        }, Charset.defaultCharset());

        Writer out = new OutputStreamWriter(implOut, Charset.defaultCharset());
        LuhnMaskers.newBasicMasker().run(inFromTestSupplier, out);
      }
    });

    Stopwatch sw = new Stopwatch().start();
    
    tests.check(inFromImpl, new TestCase.Listener() {
      @Override public void testPassed(TestCase test) {
      }

      @Override public void testFailed(TestCase test, String actualInput) {
        fail("Test #" + test.index + " of " + tests.count + " failed:"
            + "\n  Description:     " + test.description
            + "\n  Input:           " + showBreaks(test.output)
            + "\n  Expected result: " + showBreaks(test.expectedInput)
            + "\n  Actual result:   " + showBreaks(actualInput)
            + "\n");
      }
    });

    System.out.println("Took: " + sw);
  }

  static String showBreaks(String s) {
    return s.replace("\n", "\\n").replace("\r", "\\r");
  }
}
