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
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class Main {

  public static void main(String[] args) {
    LuhnMaskService service = new LuhnMaskService();
    service.run(standardInReaderSupplier(), standardOutWriter());
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
