package istic.ia.tp3;

import javax.smartcardio.Card;
import java.util.HashMap;

/**
 * @author VinYarD
 * created : 30/03/2018, 06:52
 */


public class CardMap {

    private HashMap<Integer, Card> reverse_map;

    private HashMap<String, Card> map;
    private int cpt;

    public CardMap() {
        this.map = new HashMap<>();
        this.reverse_map = new HashMap<>();
        this.cpt = 1;
    }

    public Card reverse(int num) {
        return reverse_map.get(num);
    }

    public Card getCard(String name) {
        Card c = this.map.compute(name, (k, v) -> (v == null) ? new Card(name, cpt++) : v);
        this.reverse_map.put(c.getNumber(), c);
        return this.map.compute(name, (k, v) -> (v == null) ? new Card(name, cpt++) : v);
    }

    public class Card {
        private final String name;
        private final int number;

        public String getName() {
            return name;
        }

        public int getNumber() {
            return number;
        }

        public Card(String name, int number) {
            this.name = name;
            this.number = number;
        }

        public String toString() {
            return String.valueOf(number);
        }
    }
}
