package com.thedungeonrun.cardsheet;

import com.thedungeonrun.cardsheet.carddata.CardData;
import com.thedungeonrun.cardsheet.carddata.CardType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class InputReader implements Iterable<CardData> {
    final static String HEADER_TYPE = "Type";
    final static String HEADER_NAME = "Name";
    final static String HEADER_DESCRIPTION = "Description";

    public static String[] requiredHeaders = new String[]{
        InputReader.HEADER_TYPE,
        InputReader.HEADER_NAME,
        InputReader.HEADER_DESCRIPTION,
    };

    static class CardDataIterator implements Iterator<CardData> {
        private Iterator<CSVRecord> iterator;
        private CardData current = null;

        public CardDataIterator(CSVParser parser) {
            this.iterator = parser.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public CardData next() {
            try {
                CSVRecord csvRecord = this.iterator.next();

                String typeStrRaw = csvRecord.get(InputReader.HEADER_TYPE);
                String typeStr = typeStrRaw.toLowerCase();
                CardType cardType;
                if (typeStr.equals("good")) {
                    cardType = CardType.GOOD;
                } else if (typeStr.equals("evil")) {
                    cardType = CardType.EVIL;
                } else {
                    throw new IOException("Unexpected card type: " + typeStrRaw);
                }

                return new CardData(
                        cardType,
                        csvRecord.get(InputReader.HEADER_NAME),
                        csvRecord.get(InputReader.HEADER_DESCRIPTION)
                );
            } catch (final IOException e) {
                throw new IllegalStateException(
                    e.getClass().getSimpleName() + " reading next record: " + e.toString(),
                    e
                );
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected InputReader.CardDataIterator cardDataIterator;
    protected Reader in;

    public InputReader(Reader in) throws IOException {
        this.in = in;

        CSVParser parser = CSVParser.parse(this.in, CSVFormat.newFormat(',').withQuote('"').withHeader());

        Set<String> missingHeaders = new HashSet<>();
        Map<String, Integer> headerMap = parser.getHeaderMap();
        for (String requiredHeader : requiredHeaders) {
            if (!headerMap.containsKey(requiredHeader)) {
                missingHeaders.add(requiredHeader);
            }
        }

        if (!missingHeaders.isEmpty()) {
            throw new IOException("Missing headers in input: " + String.join(", ", missingHeaders));
        }

        this.cardDataIterator = new InputReader.CardDataIterator(parser);
    }

    @Override
    public Iterator<CardData> iterator() {
        return this.cardDataIterator;
    }
}
