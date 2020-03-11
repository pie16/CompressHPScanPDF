import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {

        long time = System.currentTimeMillis();

        PDDocument inputDocument = PDDocument.load(new File("hp.pdf"));
        PDPage page = inputDocument.getPage(0);
        PDResources resources = page.getResources();
        Iterable<COSName> xObjectNames = resources.getXObjectNames();
        BufferedImage br = null;
        int width = 0;
        int height = 0;
        for (COSName cosName : xObjectNames) {
            PDXObject xObject = resources.getXObject(cosName);
            PDImageXObject pdImage = (PDImageXObject) xObject;
            width = pdImage.getWidth();
            height = pdImage.getHeight();
            br = pdImage.getImage();
        }
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        binaryImage.getGraphics().drawImage(br, 0, 0, null);

        PDDocument outputDocument = new PDDocument();
        page = new PDPage(new PDRectangle(width, height));
        outputDocument.addPage(page);
        PDImageXObject pdImage = CCITTFactory.createFromImage(outputDocument, binaryImage);
        PDPageContentStream contents = new PDPageContentStream(outputDocument, page);
        Matrix matrix = new Matrix();
        matrix.scale(width, height);
        contents.drawImage(pdImage, matrix);
        contents.close();
        outputDocument.save("hp_compress1.pdf");

        System.out.println(System.currentTimeMillis() - time);


//        ResourceCache resourceCache = inputDocument.getResourceCache();
//        resourceCache.getX
    }
}
