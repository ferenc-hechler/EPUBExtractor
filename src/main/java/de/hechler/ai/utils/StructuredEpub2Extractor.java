package de.hechler.ai.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

public class StructuredEpub2Extractor {

    public void printText(String epubFilename) {
        try (InputStream epubInputStream = new FileInputStream(epubFilename)) {
            Book book = new EpubReader().readEpub(epubInputStream);

            System.out.println("Titel: " + book.getTitle());
            System.out.println("Autoren: " + book.getMetadata().getAuthors());
            System.out.println("--------------------------------------------------");

            // Lesereihenfolge (Spine) durchlaufen
            List<SpineReference> spineReferences = book.getSpine().getSpineReferences();
            int chapterIndex = 1;

            for (SpineReference ref : spineReferences) {
                Resource resource = ref.getResource();
                String htmlContent = new String(resource.getData(), StandardCharsets.UTF_8);

                // XHTML mit Jsoup parsen
                Document doc = Jsoup.parse(htmlContent);

                System.out.println("\n=== Abschnitt " + chapterIndex++ + " (" + resource.getHref() + ") ===");

                // Gezielt Überschriften und Absätze in Reihenfolge auslesen
                for (Element el : doc.select("h1, h2, h3, p")) {
                    String text = el.text().trim();
                    if (text.isEmpty()) {
                        continue;
                    }

                    if (el.tagName().startsWith("h")) {
                        System.out.println("\n# " + text);
                    } else {
                        System.out.println(text);
                    }
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
    
    public void extract(Path epubPath, Path extractPath, boolean overwriteExisting) {
    	if (epubPath.equals(extractPath)) {
    		throw new IllegalArgumentException("Extract path must be different from EPUB path.");
    	}
    	if (Files.exists(extractPath) && !overwriteExisting) {
    		System.out.println("  -> already extracted");    		
    		return; // Skip extraction if the output file already exists and overwrite is not allowed
    	}    	
        try (InputStream epubInputStream = new FileInputStream(epubPath.toFile()); 
			 OutputStream extractOutputStream = new FileOutputStream(extractPath.toFile())) {
        	PrintStream printStream = new PrintStream(extractOutputStream, true, StandardCharsets.UTF_8.name());
            Book book = new EpubReader().readEpub(epubInputStream);

            printStream.println("Titel: " + book.getTitle());
            printStream.println("Autoren: " + book.getMetadata().getAuthors());
            printStream.println("--------------------------------------------------");

            // Lesereihenfolge (Spine) durchlaufen
            List<SpineReference> spineReferences = book.getSpine().getSpineReferences();
            int chapterIndex = 1;

            for (SpineReference ref : spineReferences) {
                Resource resource = ref.getResource();
                String htmlContent = new String(resource.getData(), StandardCharsets.UTF_8);

                // XHTML mit Jsoup parsen
                Document doc = Jsoup.parse(htmlContent);

                printStream.println("\n=== Abschnitt " + chapterIndex++ + " (" + resource.getHref() + ") ===");

                // Gezielt Überschriften und Absätze in Reihenfolge auslesen
                for (Element el : doc.select("h1, h2, h3, p")) {
                    String text = el.text().trim();
                    if (text.isEmpty()) {
                        continue;
                    }

                    if (el.tagName().startsWith("h")) {
                    	printStream.println("\n# " + text);
                    } else {
                    	printStream.println(text);
                    }
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
