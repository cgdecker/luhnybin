package com.cgdecker.luhnybin;

import java.nio.CharBuffer;

/**
 * A list of digits used for checking for a possible credit card number. May not be longer than 16
 * digits.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnyList {

  private final int[] evens;
  private final int[] odds;
  private final int[] indices;

  private int start;
  private int end;

  public LuhnyList(int maxLength) {
    this.evens = new int[maxLength];
    this.odds = new int[maxLength];
    this.indices = new int[maxLength];
  }

  private LuhnyList(LuhnyList delegate, int start) {
    this.evens = delegate.evens;
    this.odds = delegate.odds;
    this.indices = delegate.indices;

    this.start = start;
    this.end = delegate.end;
  }

  /**
   * Adds the given digit which is at the given index in the original string. If adding this digit
   * would cause the length of the list to go over 16, the first digit will be dropped.
   */
  public void addDigit(char digit, int index) {
    int number = digit - '0';
    evens[end] = number;
    odds[end] = number > 4 ? 1 + number * 2 % 10 : number * 2;

    if (end > 0) {
      evens[end] += odds[end - 1];
      odds[end] += evens[end - 1];
    }

    indices[end] = index;

    end++;

    if (length() > 16) {
      start++;
    }
  }

  /**
   * Returns the length of this list.
   */
  public int length() {
    return end - start;
  }

  /**
   * Returns a shorter list with the first (leftmost) digit dropped.
   */
  public LuhnyList dropFirst() {
    return new LuhnyList(this, start + 1);
  }

  /**
   * Masks the digits of this list in the given buffer if the number it represents may be a credit
   * card number. Returns true if the digits were masked; false otherwise.
   */
  public boolean mask(CharBuffer buffer) {
    if (mayBeCreditCardNumber()) {
      for (int i = start; i < end; i++) {
        buffer.put(indices[i], 'X');
      }
      return true;
    }
    return false;
  }

  private boolean mayBeCreditCardNumber() {
    return length() >= 14 && isLuhny();
  }

  private int sum() {
    if (length() == 0) {
      return 0;
    }

    boolean evenLength = length() % 2 == 0;
    int startDiff = start == 0 ? 0 :
        (evenLength ? evens[start - 1] : odds[start - 1]);
    return evens[end - 1] - startDiff;
  }

  private boolean isLuhny() {
    return sum() % 10 == 0;
  }

  public String toNumber(String original) {
    StringBuilder builder = new StringBuilder(length());
    for (int i = start; i < end; i++) {
      builder.append(original.charAt(indices[i]));
    }
    return builder.toString();
  }
}
