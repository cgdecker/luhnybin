package com.cgdecker.luhnybin;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnyDigit implements Iterable<LuhnyDigit> {

  private final int original;
  private final int doubleSum;
  private final int index;
  private final int digitIndex;

  /** Total from the left to this digit if this digit is not doubled (even index counting from the right). */
  private final int evenTotal;
  /** Total from the left to this digit if this digit is doubled (odd index counting from the right). */
  private final int oddTotal;

  private final LuhnyDigit left;
  private LuhnyDigit right;

  public LuhnyDigit(int original, int index, LuhnyDigit left) {
    this.original = original;
    this.index = index;
    this.digitIndex = left == null ? 0 : left.digitIndex + 1;

    this.doubleSum = original <= 4 ?
        // if no extra digits, just double
        (original * 2) :
        // else, 1 (only possible value for 10s place) plus % 10 remainder (1s place)
        (original * 2) % 10 + 1;

    // alternating
    this.evenTotal = left == null ? original : left.oddTotal + original;
    this.oddTotal = left == null ? doubleSum : left.evenTotal + doubleSum;

    this.left = left;
    if (left != null)
      left.right = this;
  }

  public int getOriginal() {
    return original;
  }

  public int getDoubleSum() {
    return doubleSum;
  }

  public int getIndex() {
    return index;
  }

  public int getDigitIndex() {
    return digitIndex;
  }

  public int length() {
    return digitIndex + 1;
  }

  public int length(LuhnyDigit start) {
    return length() - start.getDigitIndex();
  }

  public int getEvenTotal() {
    return evenTotal;
  }

  public int getOddTotal() {
    return oddTotal;
  }

  public LuhnyDigit getLeft() {
    return left;
  }

  public LuhnyDigit getRight() {
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

  @Override public Iterator<LuhnyDigit> iterator() {
    return new Iter(this);
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
}
