package istic.ia.tp3;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author VinYarD
 * created : 30/03/2018, 06:52
 */


public class Deck {

    private HashMap<CardMap.Card, Integer> cards;
    private SortedSet<Integer> set;

    public Deck() {
        this.cards = new HashMap<CardMap.Card, Integer>();
        this.set = new TreeSet<>();
    }



    public int addCard(CardMap.Card c) {
        this.set.add(c.getNumber());
        return this.cards.compute(c, (k, v) -> (v == null) ? 1 : v++);
    }

    public int size() {
        return set.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        set.forEach((i) -> sb.append(i + " "));
        return sb.toString();
    }
}
