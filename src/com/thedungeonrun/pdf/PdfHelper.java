package com.thedungeonrun.pdf;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;

import java.io.IOException;

public class PdfHelper {
    public static float getStringWidth(String str, PDFont font, float fontSize) throws IOException {
        return PdfHelper.textDistanceToPoints(font.getStringWidth(str), font, fontSize);
    }

    public static float getLineHeight(PDFont font, float fontSize) {
        final PDFontDescriptor fontDescriptor = font.getFontDescriptor();
        final float lineHeight = fontDescriptor.getAscent() - fontDescriptor.getDescent();
        return PdfHelper.textDistanceToPoints(lineHeight, font, fontSize);
    }

    public static float getTextVerticalOffset(PDFont font, float fontSize) {
        return PdfHelper.textDistanceToPoints(font.getFontDescriptor().getAscent(), font, fontSize);
    }

    protected static float textDistanceToPoints(float distance, PDFont font, float fontSize) {
        float ratio = font.getFontMatrix().getValue(0, 0) * fontSize;
        return ratio * distance;
    }
}
