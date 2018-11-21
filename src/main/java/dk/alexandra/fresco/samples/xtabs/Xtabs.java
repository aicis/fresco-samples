package dk.alexandra.fresco.samples.xtabs;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Xtabs implements Computation<List<Integer>, ProtocolBuilderNumeric> {

  /**
   * The number of bins.
   */
  public static final int NUM_BINS = 5;

  /**
   * The number of bits required to represent a bin label (0-4)
   */
  public static final int BIN_LABEL_BITS = 3;

  /**
   * The number of bits required to represent an id (this is not specified in the criteria for
   * sample programs but seems a reasonable upper bound)
   */
  public static final int ID_BITS = 10;

  public enum Ids {
    ALICE(1), BOB(2);

    private int myId;

    Ids(int myId) {
      this.myId = myId;
    }

    public int id() {
      return myId;
    }
  }

  private int[] ids;
  private int[] values;
  private int[] bins;
  private int partyId;

  private Xtabs(int partyId, int[] ids, int[] values, int[] bins) {
    this.ids = ids;
    this.values = values;
    this.bins = bins;
    this.partyId = partyId;
  }

  public static Xtabs getAliceInstance(int[] ids, int[] bins) {
    return new Xtabs(Ids.ALICE.id(), ids, null, bins);
  }

  public static Xtabs getBobInstance(int[] ids, int[] values) {
    return new Xtabs(Ids.BOB.id(), ids, values, null);
  }

  @Override
  public DRes<List<Integer>> buildComputation(ProtocolBuilderNumeric builder) {
    if (partyId != builder.getBasicNumericContext().getMyId()) {
      throw new IllegalStateException("This party should be run with id " + partyId
          + " but is using id " + builder.getBasicNumericContext().getMyId());
    }
    Numeric numeric = builder.numeric();
    // Input id columns from both parties
    List<DRes<SInt>> aliceIds = new ArrayList<>();
    List<DRes<SInt>> bobIds = new ArrayList<>();
    for (int i : ids) {
      DRes<SInt> idA = numeric.input(BigInteger.valueOf(i), Ids.ALICE.id());
      DRes<SInt> idB = numeric.input(BigInteger.valueOf(i), Ids.BOB.id());
      aliceIds.add(idA);
      bobIds.add(idB);
    }
    // Input bin and value columns
    List<DRes<SInt>> binsList = new ArrayList<>();
    List<DRes<SInt>> valuesList = new ArrayList<>();
    for (int i = 0; i < ids.length; i++) {
      BigInteger bin = (partyId == Ids.ALICE.id()) ? BigInteger.valueOf(bins[i]) : BigInteger.ZERO;
      DRes<SInt> sBin = builder.numeric().input(bin, Ids.ALICE.id());
      binsList.add(sBin);
      BigInteger value = (partyId == Ids.BOB.id()) ? BigInteger.valueOf(values[i]) : BigInteger.ZERO;
      DRes<SInt> sValue = builder.numeric().input(value, Ids.BOB.id());
      valuesList.add(sValue);
    }
    // Initialize bin labels and sums
    List<DRes<SInt>> binLabels = new ArrayList<>(NUM_BINS);
    List<DRes<SInt>> binSums = new ArrayList<>(NUM_BINS);
    for (int i = 0; i < NUM_BINS; i++) {
      binLabels.add(numeric.known(BigInteger.valueOf(i)));
      binSums.add(numeric.known(BigInteger.ZERO));
    }
    // Compute bin sums
    Comparison comparison = builder.comparison();
    for (int i = 0; i < aliceIds.size(); i++) {
      DRes<SInt> idA = aliceIds.get(i);
      for (int j = 0; j < bobIds.size(); j++) {
        DRes<SInt> idB = bobIds.get(j);
        DRes<SInt> idMatch = comparison.equals(ID_BITS, idA, idB);
        for (int k = 0; k < binLabels.size(); k++) {
          DRes<SInt> binMatch = comparison.equals(BIN_LABEL_BITS, binsList.get(i), binLabels.get(k));
          DRes<SInt> term = numeric.mult(numeric.mult(idMatch, binMatch), valuesList.get(j));
          binSums.set(k, numeric.add(term, binSums.get(k)));
        }
      }
    }
    // Output bin sums
    List<DRes<BigInteger>> binSumOuts = new ArrayList<>();
    for (DRes<SInt> sum : binSums) {
      binSumOuts.add(numeric.open(sum));
    }
    // Unwrap results
    return () -> binSumOuts.stream().map(DRes::out).map(BigInteger::intValue)
        .collect(Collectors.toList());
  }

}
