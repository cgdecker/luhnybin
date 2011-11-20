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

    int lastIndex = prev(end);

    evens[end] = number + odds[lastIndex];
    odds[end] = DOUBLE_SUMS[number] + evens[lastIndex];

    indices[end] = index;

    end = next(end);

    if (length < 16) {
      length++;
    } else {
      start = next(start);
    }
  }

  /**
   * Gets the number of digits in the buffer.
   */
  public int length() {
    return length;
  }

  /**
   * Gets the buffer index before {@code i}.
   */
  private static int prev(int i) {
    return wrap(i - 1);
  }

  /**
   * Gets the buffer index after {@code i}.
   */
  private static int next(int i) {
    return wrap(i + 1);
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
          start = next(start);
          length--;
        }
      }
    } finally {
      start = originalStart;
      length = originalLength;
    }
  }

  private void maskUnmaskedDigits(char[] buffer) {
    for (int i = start; i != end; i = next(i)) {
      if (!mask(buffer, i))
        break;
    }

    for (int i = prev(end); i != prev(start); i = prev(i)) {
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
    int totalSum = evens[prev(end)];
    int indexBeforeStart = prev(start);
    int sumToSubtract = length % 2 == 0 ? evens[indexBeforeStart] : odds[indexBeforeStart];
    return totalSum - sumToSubtract;
  }
}
