package com.thedungeonrun.cardsheet;

import com.thedungeonrun.cardsheet.carddata.CardData;
import com.thedungeonrun.cardsheet.carddata.CardType;
import com.thedungeonrun.pdf.FittedText;
import com.thedungeonrun.pdf.PdfHelper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

public class CardDocument {
    public static final int CARDS_PER_PAGE = 4;

    public static final float CARD_WIDTH = 350;
    public static final float CARD_HEIGHT = 170;
    public static final float CARD_GUTTER = 15;

    public static final float IMAGE_SCALE = 0.225f;
    public static final String GOOD_IMAGE_FILENAME = "TDG-Dragon Black.png";
    public static final String EVIL_IMAGE_FILENAME = "TDG-Dragon Red.png";

    public static final String TITLE_FONT = "Arial Bold";
    public static final float TITLE_DEFAULT_FONT_SIZE = 22;

    public static final String BODY_FONT = "Arial";
    public static final float BODY_DEFAULT_FONT_SIZE = 14;

    protected PDDocument document = new PDDocument();
    protected PDPage currentPage = null;
    protected int currentCardIndex = 0;
    protected CardType[] cardTypes = new CardType[]{null, null, null, null};

    protected PDImageXObject goodImage = null;
    protected PDImageXObject evilImage = null;

    protected PDFont titleFont = null;
    protected PDFont bodyFont = null;

    protected PDImageXObject getBackImage(CardType cardType) throws IOException {
        if (cardType == CardType.GOOD) {
            if (this.goodImage == null) {
                this.goodImage = PDImageXObject.createFromFile(CardDocument.GOOD_IMAGE_FILENAME, this.document);
            }
            return this.goodImage;
        }

        if (this.evilImage == null) {
            this.evilImage = PDImageXObject.createFromFile(CardDocument.EVIL_IMAGE_FILENAME, this.document);
        }
        return this.evilImage;
    }

    protected PDFont getTitleFont() throws IOException {
        if (this.goodImage == null) {
            this.titleFont = PDType0Font.load(this.document, new File(CardDocument.TITLE_FONT + ".ttf"));
        }
        return this.titleFont;
    }

    protected PDFont getBodyFont() throws IOException {
        if (this.goodImage == null) {
            this.bodyFont = PDType0Font.load(this.document, new File(CardDocument.BODY_FONT + ".ttf"));
        }
        return this.bodyFont;
    }

    protected void addCard(CardData card) {
        if (this.currentPage == null) {
            this.currentPage = new PDPage();
        }

        this.cardTypes[this.currentCardIndex] = card.type;

        Rectangle2D.Float area = this.getCardArea(this.currentPage, this.currentCardIndex);

        try (PDPageContentStream contents = new PDPageContentStream(this.document, this.currentPage, PDPageContentStream.AppendMode.APPEND, true, true))
        {
            PDFont titleFont = this.getTitleFont();
            float titleHeight = PdfHelper.getLineHeight(titleFont, CardDocument.TITLE_DEFAULT_FONT_SIZE);

            Rectangle2D.Float titleArea = new Rectangle2D.Float(
                area.x + CardDocument.CARD_GUTTER,
                area.y + area.height - titleHeight - CardDocument.CARD_GUTTER,
                area.width - 2 * CardDocument.CARD_GUTTER,
                titleHeight
            );
            this.addTextToArea(card.name, titleFont, CardDocument.TITLE_DEFAULT_FONT_SIZE, titleArea, contents);

            Rectangle2D.Float bodyArea = new Rectangle2D.Float(
                area.x + CardDocument.CARD_GUTTER,
                area.y + CardDocument.CARD_GUTTER,
                    area.width - 2 * CardDocument.CARD_GUTTER,
                area.height - titleHeight - 3 * CardDocument.CARD_GUTTER
            );
            this.addTextToArea(card.description, this.getBodyFont(), CardDocument.BODY_DEFAULT_FONT_SIZE, bodyArea, contents);

            contents.setStrokingColor(Color.BLACK);
            contents.addRect(area.x, area.y, area.width, area.height);
            contents.stroke();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (++this.currentCardIndex == CARDS_PER_PAGE) {
            this.finishPage();
        }
    }

    protected void addTextToArea(String str, PDFont font, float defaultFontSize, Rectangle2D.Float area, PDPageContentStream contents) throws IOException {
        final FittedText text = FittedText.calculate(str, font, area, defaultFontSize, defaultFontSize * 0.5f);

        float lineHeight = PdfHelper.getLineHeight(font, text.fontSize);
        float lineOffset = PdfHelper.getTextVerticalOffset(font, text.fontSize);

        float y = area.y + area.height;

        for (String line : text.lines) {
            float lineWidth = PdfHelper.getStringWidth(line, font, text.fontSize);
            float x = area.x + (area.width - lineWidth) / 2;

            contents.beginText();
            contents.setFont(font, text.fontSize);
            contents.newLineAtOffset(x, y - lineOffset);
            contents.showText(line);
            contents.endText();

            y -= lineHeight;
        }
    }

    protected void finishPage() {
        PDPage backPage = new PDPage();

        for (int i = 0; i < this.currentCardIndex; i++) {
            CardType cardType = this.cardTypes[i];
            Rectangle2D.Float area = this.getCardArea(backPage, i);

            try (PDPageContentStream contentStream = new PDPageContentStream(this.document, backPage, PDPageContentStream.AppendMode.APPEND, true, true))
            {
                PDImageXObject image = this.getBackImage(cardType);

                float width = image.getWidth() * CardDocument.IMAGE_SCALE;
                float height = image.getHeight() * CardDocument.IMAGE_SCALE;
                float x = area.x + (area.width - width) / 2;
                float y = area.y + (area.height - height) / 2;

                contentStream.drawImage(image, x, y, width, height);

                contentStream.setStrokingColor(Color.BLACK);
                contentStream.addRect(area.x, area.y, area.width, area.height);
                contentStream.stroke();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.document.addPage(backPage);
        this.document.addPage(this.currentPage);

        this.currentPage = null;
        this.currentCardIndex = 0;
    }

    protected Rectangle2D.Float getCardArea(PDPage page, int cardIndex) {
        final PDRectangle mediaBox = page.getMediaBox();
        float x = mediaBox.getLowerLeftX() + (mediaBox.getWidth() - CardDocument.CARD_WIDTH) / 2;
        float y = mediaBox.getUpperRightY() - (mediaBox.getHeight() - CardDocument.CARDS_PER_PAGE * CardDocument.CARD_HEIGHT) / 2 - CardDocument.CARD_HEIGHT * (1 + cardIndex);

        return new Rectangle2D.Float(x, y, CardDocument.CARD_WIDTH, CardDocument.CARD_HEIGHT);
    }

    protected void save(String filename) throws IOException {
        if (this.currentCardIndex != 0) {
            this.finishPage();
        }

        this.document.save(filename);
    }
}
