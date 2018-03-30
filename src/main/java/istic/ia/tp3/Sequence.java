package istic.ia.tp3;

import java.util.ArrayList;

/**
 * @author VinYarD
 * created : 30/03/2018, 19:48
 */


public class Sequence {


    private ArrayList<Deck> set;

    public Sequence() {
        this.set = new ArrayList<>();
    }

    public void addSequence(Deck d) {
        this.set.add(d);
    }

    public int size() {
        return set.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        set.forEach((i) -> sb.append(i.toString() + "-1 "));
        sb.append("-2");
        return sb.toString();
    }
}
