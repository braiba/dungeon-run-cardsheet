package com.thedungeonrun.cardsheet.carddata;

public class CardData {
    public final CardType type;
    public final String name;
    public final String description;

    public CardData(CardType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }
}
