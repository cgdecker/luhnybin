package com.cgdecker.luhnybin;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnyLineWriterTest {

  @Test public void testValid14DigitNumber() throws IOException {
    assertMask("56613959932537", "XXXXXXXXXXXXXX");
  }

  @Test public void testValid16DigitNumber() {
    assertMask("6853371389452376", "XXXXXXXXXXXXXXXX");
  }

  @Test public void testLineFeedOnly() throws IOException {
    assertMask("LF only ->", "LF only ->");
    assertMask("<- LF only", "<- LF only");
  }

  @Test public void testTooManyDigits() {
    assertMask("99929316122852072", "99929316122852072");
  }

  @Test public void test16DigitFlankedByNonMatching() {
    assertMask("9875610591081018250321", "987XXXXXXXXXXXXXXXX321");
  }

  @Test public void exceptionMessageContainingACard() {
    assertMask("java.lang.FakeException: 7230 3161 3748 4124 is a card #.",
        "java.lang.FakeException: XXXX XXXX XXXX XXXX is a card #.");
  }

  @Test public void valid14DigitAtEndOfInvalid16Digit() {
    assertMask("1256613959932537", "12XXXXXXXXXXXXXX");
  }

  @Test public void twoValid14DigitsInARow() {
    assertMask("5661395993253756613959932537", "XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
  }

  private static void assertMask(String in, String expectedOut) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out, true);
    try {
      LuhnyLineWriter.process(in, writer);
      writer.flush();
      out.flush();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    assertEquals(expectedOut + '\n', getOutput(out.toByteArray()));
  }

  private static String getOutput(byte[] bytes) {
    try {
      return CharStreams.toString(
          CharStreams.newReaderSupplier(ByteStreams.newInputStreamSupplier(bytes),
              Charset.defaultCharset()));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
