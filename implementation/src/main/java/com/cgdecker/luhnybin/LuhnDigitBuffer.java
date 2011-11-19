package com.cgdecker.luhnybin;

/**
 * A list of digits used for checking for a possible credit card number. May not be longer than 16
 * digits.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
final class LuhnDigitBuffer {

  private static final int[] DOUBLE_SUMS = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};

  private static final int BUFFER_SIZE = 32;

  private final int[] evens = new int[BUFFER_SIZE];
  private final int[] odds = new int[BUFFER_SIZE];
  private final int[] indices = new int[BUFFER_SIZE];

  private int start;
  private int end;
  private int length;

  /**
   * Resets the length to 0 and previous sums to 0.
   */
  public void reset() {
    start = 0;
    end = 0;
    length = 0;

    // reset these so the sum at the index before start is 0
    evens[BUFFER_SIZE - 1] = 0;
    odds[BUFFER_SIZE - 1] = 0;
  }

  /**
   * Adds the given digit which is at the given index in the original string. If adding this digit
   * would cause the length of the list to go over 16, the first digit will be dropped.
   */
  public void add(char digit, int index) {
    int number = digit - '0';

    int lastIndex = wrap(end - 1);

    evens[end] = number + odds[lastIndex];
    odds[end] = DOUBLE_SUMS[number] + evens[lastIndex];

    indices[end] = index;

    end = wrap(end + 1);

    if (length < 16) {
      length++;
    } else {
      start = wrap(start + 1);
    }
  }

  /**
   * Gets the number of digits in the buffer.
   */
  public int length() {
    return length;
  }

  private static int wrap(int i) {
    return i & BUFFER_SIZE - 1;
  }

  /**
   * For the digits this list represents and any shorter list of digits that ends at the digit this
   * list ends at, masks the digits if they pass the Luhn check and may be a credit card number.
   */
  public void mask(char[] buffer) {
    int originalStart = start;
    int originalLength = length;
    try {
      while (length >= 14) {
        if (isLuhny()) {
          maskUnmaskedDigits(buffer);
          return;
        } else {
          start = wrap(start + 1);
          length--;
        }
      }
    } finally {
      start = originalStart;
      length = originalLength;
    }
  }

  private void maskUnmaskedDigits(char[] buffer) {
    for (int i = start; i != end; i = wrap(i + 1)) {
      if (!mask(buffer, i))
        break;
    }

    for (int i = wrap(end - 1); i != wrap(start - 1); i = wrap(i - 1)) {
      if (!mask(buffer, i))
        break;
    }
  }

  private boolean mask(char[] buffer, int i) {
    int index = indices[i];
    if (buffer[index] != 'X') {
      buffer[index] = 'X';
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
    int totalSum = evens[wrap(end - 1)];
    int indexBeforeStart = wrap(start - 1);
    int sumToSubtract = length % 2 == 0 ? evens[indexBeforeStart] : odds[indexBeforeStart];
    return totalSum - sumToSubtract;
  }
}
