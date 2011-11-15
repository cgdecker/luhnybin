package com.cgdecker.luhnybin;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.junit.Before;
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

  private static void assertMask(String in, String expectedOut) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out, true);
    new LuhnyLineWriter(in, writer).process();
    writer.flush();
    try {
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
