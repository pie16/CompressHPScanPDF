import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CompressPDF {
    private CompressPDF() {
    }

    public static void main(String[] args) {

        long time = System.currentTimeMillis();

        if (args == null || args.length == 0) {
            System.out.println("No argument");
            return;
        }

        String inputFile = args[0];

        int endIndex = inputFile.lastIndexOf(".pdf");
        if (endIndex == -1){
            System.out.println("Error, incorrect file name");
            return;
        }

        String outputFile = inputFile.substring(0, endIndex) + "_compress.pdf";

        try (FileInputStream fileInputStream = new FileInputStream(inputFile);
             //Loading an existing document
             PDDocument inputDocument = PDDocument.load(fileInputStream, MemoryUsageSetting.setupMixed(10));
             PDDocument outputDocument = new PDDocument())
        {

            PDFRenderer renderer = new PDFRenderer(inputDocument);

            for (int pageNumber = 0; pageNumber < inputDocument.getNumberOfPages(); pageNumber++) {

                //Get input document width and height
                PDRectangle cropBox = inputDocument.getPage(pageNumber).getCropBox();
                float width = cropBox.getWidth();
                float height = cropBox.getHeight();

                //Adding a page to the document
                PDPage page = new PDPage(new PDRectangle(width, height));
                outputDocument.addPage(page);

                BufferedImage binaryImage = ExtractingBinaryImage(renderer, pageNumber);

                PDImageXObject pdImage = CCITTFactory.createFromImage(outputDocument, binaryImage);

                PDPageContentStream contents = new PDPageContentStream(outputDocument, page);
                Matrix matrix = new Matrix();
                matrix.scale(width, height);
                contents.drawImage(pdImage, matrix);
                contents.close();
            }
            outputDocument.save(outputFile);

        } catch (FileNotFoundException e) {
            System.out.println("Ошибка чтения/записи файла");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - time);
    }

    private static BufferedImage ExtractingBinaryImage(PDFRenderer renderer, int pageNumber) throws IOException {

        BufferedImage image = renderer.renderImageWithDPI(pageNumber, 300, ImageType.GRAY);

        //вместо передачи в renderImageWithDPI ImageType.BINARY,
        // создаем новый BufferedImage с imageType - TYPE_BYTE_BINARY, так итоговая картинка будет лучше
        BufferedImage binaryImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        binaryImage.getGraphics().drawImage(image, 0, 0, null);

        return binaryImage;
    }
}
