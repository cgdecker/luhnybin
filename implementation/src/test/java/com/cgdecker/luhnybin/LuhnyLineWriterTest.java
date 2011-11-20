package com.cgdecker.luhnybin;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  @Test public void longSeriesOfDigitsWithNoMatches() {
    assertMask("6312638493661363789472853119005759533684587544905521619973644932068188360230935532527154743335848839828851725990500810593000543831317039087810708642627656248593831467340319746493667735850698061656232601253373883359630621836093141133991929057879765926391300406046612472508020764791071576643968751949234096676834526277439709984870269141329285061574782953237760758295720941151514468351845775514483469687366714407861921160700515433541143929484443589677725808211529690117826740565406860603578795506374754938886883394184975357884774215036729806632553736333309563850022252781000711011148711394078414811937738780731241621683795853521771664147013018034521271169167731506107805861547087691469801680542097550235003841947219728890046861059486726595366379845969363514941276722650021497487336440464577768279961313965853790009235325434748508904052465204408049513481666570134026749562373843891353223425778914829516173676629660442526568660809351338271262538718112151213388955059832308272340118506811182287868286077699",
        "6312638493661363789472853119005759533684587544905521619973644932068188360230935532527154743335848839828851725990500810593000543831317039087810708642627656248593831467340319746493667735850698061656232601253373883359630621836093141133991929057879765926391300406046612472508020764791071576643968751949234096676834526277439709984870269141329285061574782953237760758295720941151514468351845775514483469687366714407861921160700515433541143929484443589677725808211529690117826740565406860603578795506374754938886883394184975357884774215036729806632553736333309563850022252781000711011148711394078414811937738780731241621683795853521771664147013018034521271169167731506107805861547087691469801680542097550235003841947219728890046861059486726595366379845969363514941276722650021497487336440464577768279961313965853790009235325434748508904052465204408049513481666570134026749562373843891353223425778914829516173676629660442526568660809351338271262538718112151213388955059832308272340118506811182287868286077699");
  }

  private static void assertMask(String in, String expectedOut) {
    String result;
    try {
      result = new String(LuhnLineMasker.mask(in));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    assertEquals(expectedOut + '\n', result + '\n');
  }
}
