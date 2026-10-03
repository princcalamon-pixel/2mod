
package com.suveren.feudal.util;

import java.util.Random;

public class KnightNames {
    private static final String[] KNIGHTS = {
        "Branimir", "Vojnomir", "Ljudmil", "Svetoslav", "Dragomir",
        "Bogomil", "Zvonimir", "Radomir", "Milos", "Stanislav", "Grgur", "Avgustin"
    };
    private static final String[] BANDITS = {
        "Grozd", "Crni Jaka", "Hudicev Rok", "Brezimni", "Krvavi Miha", "Sivi Tomaz"
    };
    private static final String[] FARMERS = {
        "Kmet Ozbej", "Kmeta Neza", "Kmet Vid", "Kmeta Minka",
        "Kmet Janez", "Kmeta Pepca", "Kmet Mihel", "Kmeta Ana"
    };

    public static String knight(Random r) { return "Vitez " + KNIGHTS[r.nextInt(KNIGHTS.length)]; }
    public static String bandit(Random r) { return "Razbojnik " + BANDITS[r.nextInt(BANDITS.length)]; }
    public static String farmer(Random r) { return FARMERS[r.nextInt(FARMERS.length)]; }
}
