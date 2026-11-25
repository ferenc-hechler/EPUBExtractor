package de.hechler.ai.utils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfTextExtractor {

    public void extract(Path pdfPath, Path extractPath, boolean overwriteExisting) {
    	if (pdfPath.equals(extractPath)) {
    		throw new IllegalArgumentException("Extract path must be different from PDF path.");
    	}
    	if (Files.exists(extractPath) && !overwriteExisting) {
    		System.out.println("  -> already extracted");    		
    		return; // Skip extraction if the output file already exists and overwrite is not allowed
    	}
        // In PDFBox 3.x wird Loader.loadPDF(...) verwendet
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile());
        		OutputStream extractOutputStream = new FileOutputStream(extractPath.toFile())) {
        	PrintStream printStream = new PrintStream(extractOutputStream, true, StandardCharsets.UTF_8.name());
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); // Sortiert Text nach Position auf der Seite

            int totalPages = document.getNumberOfPages();
            printStream.println("Seitenanzahl: " + totalPages);
            printStream.println("--------------------------------------------------");

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(document).trim();
                if (!pageText.isEmpty()) {
                	printStream.println("\n=== Seite " + page + " ===");
                	printStream.println(pageText);
                }
            }
        }
        catch (RuntimeException e) {
        	throw e;
        }
        catch (Exception e) {
			throw new RuntimeException(e);
        }
    }
}
