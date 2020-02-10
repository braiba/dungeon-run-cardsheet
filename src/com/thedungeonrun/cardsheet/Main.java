package com.thedungeonrun.cardsheet;

import com.thedungeonrun.cardsheet.carddata.CardData;

import java.io.FileReader;

public class Main {

    public static void main(String[] args) {
        // String filename = args[0];

        try {
            InputReader in = new InputReader(new FileReader("sample-input.csv"));
            CardDocument out = new CardDocument();

            for (CardData card : in) {
                out.addCard(card);
            }

            out.save("sample-output.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
