package com.cgdecker.luhnybin;

import com.google.common.collect.AbstractIterator;

import java.nio.CharBuffer;
import java.util.Iterator;

/**
 * A list of digits used for checking for a possible credit card number. May not be longer than 16
 * digits.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnyList {

  private LuhnyDigit start;
  private LuhnyDigit head;

  /**
   * Adds the given digit which is at the given index in the original string. If adding this digit
   * would cause the length of the list to go over 16, the first digit will be dropped.
   */
  public void addDigit(char digit, int index) {
    head = new LuhnyDigit(digit - '0', index, head);

    if (start == null) {
      start = head;
    }

    if (head.length(start) > 16) {
      start = start.right();
    }
  }

  /**
   * Returns the length of this list.
   */
  public int length() {
    return head.length(start);
  }

  /**
   * Returns a shorter list with the first (leftmost) digit dropped.
   */
  public LuhnyList dropFirst() {
    LuhnyList result = new LuhnyList();
    result.start = start.right();
    result.head = head;
    return result;
  }

  /**
   * Masks the digits of this list in the given buffer if the number it represents may be a credit
   * card number. Returns true if the digits were masked; false otherwise.
   */
  public boolean mask(CharBuffer buffer) {
    if (mayBeCreditCardNumber()) {
      for (LuhnyDigit digit : asIterable()) {
        buffer.put(digit.index(), 'X');
      }
      return true;
    }
    return false;
  }

  private boolean mayBeCreditCardNumber() {
    return head.length() >= 14 && head.isLuhny(start);
  }

  private Iterable<LuhnyDigit> asIterable() {
    return new Iterable<LuhnyDigit>() {
      @Override public Iterator<LuhnyDigit> iterator() {
        return new Iter(start);
      }
    };
  }

  private class Iter extends AbstractIterator<LuhnyDigit> {
    private LuhnyDigit current;

    public Iter(LuhnyDigit start) {
      this.current = start;
    }

    @Override protected LuhnyDigit computeNext() {
      if (current == null) {
        return endOfData();
      }
      LuhnyDigit result = current;
      current = current.right;
      return result;
    }
  }

  /**
   * A linked list node representing a digit in a string. Contains the value of the digit, its
   * original index in the string and all the information needed to quickly determine if the
   * sequence of numbers starting at another digit and ending at this one passes the Luhn check.
   */
  private static class LuhnyDigit {

    private final int digit;
    private final int index;
    private final int digitIndex;

    /**
     * Total from the left to this digit if this digit is not doubled (even index counting from the right).
     */
    private final int evenTotal;
    /**
     * Total from the left to this digit if this digit is doubled (odd index counting from the right).
     */
    private final int oddTotal;

    private final LuhnyDigit left;
    private LuhnyDigit right;

    public LuhnyDigit(int digit, int index, LuhnyDigit left) {
      this.digit = digit;
      this.index = index;
      this.digitIndex = left == null ? 0 : left.digitIndex + 1;

      // alternating
      this.evenTotal = left == null ? digit : left.oddTotal + digit;
      this.oddTotal = left == null ? doubleDigitSum() : left.evenTotal + doubleDigitSum();

      this.left = left;
      if (left != null)
        left.right = this;
    }

    private int doubleDigitSum() {
      return digit <= 4 ?
          // if no extra digits, just double
          (digit * 2) :
          // else, 1 (only possible value for 10s place) + % 10 remainder (1s place)
          (digit * 2) % 10 + 1;
    }

    public int index() {
      return index;
    }

    public int digitIndex() {
      return digitIndex;
    }

    public int length() {
      return digitIndex + 1;
    }

    public int length(LuhnyDigit start) {
      return length() - start.digitIndex();
    }

    public LuhnyDigit right() {
      return right;
    }

    public boolean isLuhny() {
      return evenTotal % 10 == 0;
    }

    public boolean isLuhny(LuhnyDigit leftmostDigit) {
      if (leftmostDigit.left == null) {
        return isLuhny();
      }

      boolean leftmostIsEven = (length(leftmostDigit) - 1) % 2 == 0;
      int amountToSubtract = leftmostIsEven ?
          leftmostDigit.left.oddTotal :
          leftmostDigit.left.evenTotal;

      int total = evenTotal - amountToSubtract;
      return total % 10 == 0;
    }
  }
}
