package dk.alexandra.fresco.samples.mult3;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MultThree implements Application<Integer, ProtocolBuilderNumeric> {

  private final int factor;

  public MultThree(int factor) {
    this.factor = factor;
  }

  @Override
  public DRes<Integer> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> sFactors = new ArrayList<>(3);
    // Input factors
    for (int i = 1; i <= 3; i++) {
      DRes<SInt> sFactor = builder.numeric().input(BigInteger.valueOf(factor), i);
      sFactors.add(sFactor);
    }
    // Multiply numbers
    DRes<SInt> temp = builder.numeric().mult(sFactors.get(0), sFactors.get(1));
    DRes<SInt> result = builder.numeric().mult(temp, sFactors.get(2));
    // Open the result
    DRes<BigInteger> openResult = builder.numeric().open(result);
    return () -> openResult.out().intValue();
  }

}
