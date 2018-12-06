package dk.alexandra.fresco.samples.mult3;

import static dk.alexandra.fresco.samples.Id.*;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class MultThree implements Application<Integer, ProtocolBuilderNumeric> {

  private final int factor;

  public MultThree(int factor) {
    this.factor = factor;
  }

  @Override
  public DRes<Integer> buildComputation(ProtocolBuilderNumeric builder) {
    // Input factors
    DRes<SInt> factA = builder.numeric().input(BigInteger.valueOf(factor), ALICE.id());
    DRes<SInt> factB = builder.numeric().input(BigInteger.valueOf(factor), BOB.id());
    DRes<SInt> factC = builder.numeric().input(BigInteger.valueOf(factor), CAROL.id());
    // Multiply numbers
    DRes<SInt> temp = builder.numeric().mult(factA, factB);
    DRes<SInt> result = builder.numeric().mult(temp, factC);
    // Open the result
    DRes<BigInteger> openResult = builder.numeric().open(result);
    return () -> openResult.out().intValue();
  }

}
