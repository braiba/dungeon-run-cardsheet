package com.thedungeonrun.pdf;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class FittedText {
    public final String[] lines;
    public final float fontSize;

    protected FittedText(String[] lines, float fontSize) {
        this.lines = lines;
        this.fontSize = fontSize;
    }

    public static FittedText calculate(String text, PDFont font, Rectangle2D.Float area, float defaultFontSize, float minFontSize) throws IOException {
        String[] rawLines = text.split("(\r\n|\n)");

        float fontSize = defaultFontSize;
        while (fontSize >= minFontSize) {
            String[] lines = FittedText.split(rawLines, font, area.width, fontSize);
            float lineHeight = PdfHelper.getLineHeight(font, fontSize);
            int maxLines = (int) Math.floor(area.height / lineHeight);

            if (lines.length <= maxLines) {
                return new FittedText(lines, fontSize);
            }

            fontSize -= 1.0f;
        }

        throw new IOException("Not possible to split text successfully");
    }

    protected static String[] split(String[] rawLines, PDFont font, float maxWidth, float fontSize) throws IOException {
        List<String> lineList = new Vector<>();

        for (String line : rawLines) {
            if (PdfHelper.getStringWidth(line, font, fontSize) <= maxWidth) {
                lineList.add(line);
                continue;
            }

            String[] words = line.split(" ");
            String currentLineOld = "";
            String currentLineNew;
            for (String word : words) {
                currentLineNew = currentLineOld.concat((currentLineOld.isEmpty() ? "" : " ") + word);

                if (PdfHelper.getStringWidth(currentLineNew, font, fontSize) > maxWidth) {
                    lineList.add(currentLineOld);
                    currentLineOld = word;
                } else {
                    currentLineOld = currentLineNew;
                }
            }

            if (!currentLineOld.isEmpty()) {
                lineList.add(currentLineOld);
            }
        }

        String[] lines = new String[lineList.size()];
        lineList.toArray(lines);
        return lines;
    }
}
