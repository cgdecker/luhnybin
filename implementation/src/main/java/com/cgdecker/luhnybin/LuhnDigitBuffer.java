package com.cgdecker.luhnybin;

/**
 * A list of digits used for checking for a possible credit card number. May not be longer than 16
 * digits.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
final class LuhnDigitBuffer {

  private static final int[] DOUBLE_SUMS = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};

  private final int[] evens;
  private final int[] odds;
  private final int[] indices;

  private int start;
  private int end;

  public LuhnDigitBuffer(int maxLength) {
    this.evens = new int[maxLength];
    this.odds = new int[maxLength];
    this.indices = new int[maxLength];
  }

  /**
   * Adds the given digit which is at the given index in the original string. If adding this digit
   * would cause the length of the list to go over 16, the first digit will be dropped.
   */
  public void add(char digit, int index) {
    int number = digit - '0';
    evens[end] = number;
    odds[end] = DOUBLE_SUMS[number];

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
   * For the digits this list represents and any shorter list of digits that ends at the digit this
   * list ends at, masks the digits if they pass the Luhn check and may be a credit card number.
   */
  public void mask(char[] buffer) {
    int originalStart = start;
    try {
      while (length() >= 14) {
        if (isLuhny()) {
          maskUnmaskedDigits(buffer);
          return;
        } else {
          start++;
        }
      }
    } finally {
      start = originalStart;
    }
  }

  private void maskUnmaskedDigits(char[] buffer) {
    for (int i = start; i < end; i++) {
      if (!mask(buffer, i))
        break;
    }

    for (int i = end - 1; i >= start; i--) {
      if (!mask(buffer, i))
        break;
    }
  }

  private boolean mask(char[] buffer, int i) {
    if (buffer[indices[i]] != 'X') {
      buffer[indices[i]] = 'X';
      return true;
    } else {
      return false;
    }
  }

  private boolean isLuhny() {
    return sum() % 10 == 0;
  }

  private int sum() {
    // ignore case where length is 0... only called when length is 14+
    int result = evens[end - 1];
    if (start > 0) {
      int sumToSubtract = length() % 2 == 0 ?
          evens[start - 1] : odds[start - 1];
      result -= sumToSubtract;
    }
    return result;
  }
}
