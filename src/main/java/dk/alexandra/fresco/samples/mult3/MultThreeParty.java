package dk.alexandra.fresco.samples.mult3;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.samples.SpdzParty;
import java.io.IOException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultThreeParty {

  private static final Logger logger = LoggerFactory.getLogger(MultThreeParty.class);

  private static final int FACTOR_A = 10;
  private static final int FACTOR_B = 20;
  private static final int FACTOR_C = 30;

  public static void main(String[] args) throws IOException {
    int partyId = partyIdFromArgs(args);
    logInfo();
    HashMap<Integer, Party> parties = new HashMap<>(3);
    parties.put(1, new Party(1, "localhost", 12000));
    parties.put(2, new Party(2, "localhost", 12001));
    parties.put(3, new Party(3, "localhost", 12002));
    NetworkConfiguration netConf = new NetworkConfigurationImpl(partyId, parties);
    SpdzParty party = SpdzParty.builder(netConf).build();
    CloseableNetwork net = party.getNetwork();
    int result;
    if (partyId == 1) {
      result = party.getSce().runApplication(new MultThree(FACTOR_A),  party.getRp(), net);
    } else if (partyId == 2) {
      result = party.getSce().runApplication(new MultThree(FACTOR_B),  party.getRp(), net);
    } else {
      result = party.getSce().runApplication(new MultThree(FACTOR_C),  party.getRp(), net);
    }
    party.getSce().shutdownSCE();
    net.close();
    logger.info("Got result: " + result);
  }

  private static int partyIdFromArgs(String[] args) {
    int myId = -1;
    if (args.length != 1) {
      System.err.println("A single argument is needed indicating the party id. Arguments given were \"" + String.join(" ", args) + "\"");
      System.exit(1);
    }
    try {
      myId = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse argument \"" + args[0] + "\" as an integer (this argument should specify the party id)");
      System.exit(1);
    }
    if (!(myId == 1 || myId == 2 || myId == 3)) {
      System.err.println("Party id must be either 1, 2 or 3, but was " + myId);
      System.exit(1);
    }
    return myId;
  }

  private static void logInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n"
        +
        "    __________  ___________ __________     __  ___      ____ _____    ____\n" +
        "   / ____/ __ \\/ ____/ ___// ____/ __ \\   /  |/  /_  __/ / /|__  /   / __ \\___  ____ ___  ____\n" +
        "  / /_  / /_/ / __/  \\__ \\/ /   / / / /  / /|_/ / / / / / __//_ <   / / / / _ \\/ __ `__ \\/ __ \\\n" +
        " / __/ / _, _/ /___ ___/ / /___/ /_/ /  / /  / / /_/ / / /____/ /  / /_/ /  __/ / / / / / /_/ /\n" +
        "/_/   /_/ |_/_____//____/\\____/\\____/  /_/  /_/\\__,_/_/\\__/____/  /_____/\\___/_/ /_/ /_/\\____/\n");
    sb.append("\nComputing Mult3 on inputs:\n");
    sb.append("Factor a: " + FACTOR_A + "\n");
    sb.append("Factor b: " + FACTOR_B + "\n");
    sb.append("Factor c: " + FACTOR_C + "\n");
    logger.info("{}", sb.toString());
  }

}
