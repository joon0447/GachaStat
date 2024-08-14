package org.joon.gachastat.Manager;

import java.util.HashMap;

public class StatManager {

    public HashMap<String, Double> statAttack;
    public HashMap<String, Double> statHp;
    public HashMap<String, Double> statDefense;

    public StatManager() {
        this.statAttack = new HashMap<>();
        this.statHp = new HashMap<>();
        this.statDefense = new HashMap<>();
    }


}
