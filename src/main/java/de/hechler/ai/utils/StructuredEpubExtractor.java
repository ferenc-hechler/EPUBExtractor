package de.hechler.ai.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class StructuredEpubExtractor {
	
    public void extract(Path epubPath, Path extractPath) {
    	if (epubPath.equals(extractPath)) {
    		throw new IllegalArgumentException("Extract path must be different from EPUB path.");
    	}
        try (ZipFile zipFile = new ZipFile(epubPath.toFile());
        	OutputStream extractOutputStream = new FileOutputStream(extractPath.toFile())) {
        	PrintStream printStream = new PrintStream(extractOutputStream, true, StandardCharsets.UTF_8.name());
        	
            // 1. Pfad zur .opf-Datei aus META-INF/container.xml ermitteln
            String opfPath = getOpfPath(zipFile);
            String baseDir = opfPath.contains("/") 
                    ? opfPath.substring(0, opfPath.lastIndexOf('/') + 1) 
                    : "";

            // 2. .opf-Datei als XML parsen
            Document opfDoc = parseXmlFromZip(zipFile, opfPath);

            String title = opfDoc.select("metadata > dc|title, metadata > title").text();
            String author = opfDoc.select("metadata > dc|creator, metadata > creator").text();
            printStream.println("Titel: " + title);
            printStream.println("Autor: " + author);
            printStream.println("--------------------------------------------------");

            // 3. Manifest einlesen (ID -> Dateipfad)
            Map<String, String> manifest = new HashMap<>();
            for (Element item : opfDoc.select("manifest > item")) {
                manifest.put(item.attr("id"), item.attr("href"));
            }

            // 4. Spine (Lesereihenfolge) durchlaufen
            int chapterIndex = 1;
            for (Element itemref : opfDoc.select("spine > itemref")) {
                String idref = itemref.attr("idref");
                String href = manifest.get(idref);
                if (href == null) continue;

                // URL-Encoding im Pfad bereinigen (z. B. %20) und Basispfad voranstellen
                String fullPath = baseDir + java.net.URLDecoder.decode(href, StandardCharsets.UTF_8);
                ZipEntry entry = zipFile.getEntry(fullPath);
                if (entry == null) continue;

                try (InputStream is = zipFile.getInputStream(entry)) {
                    String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Document chapterDoc = Jsoup.parse(html);

                    printStream.println("\n=== Abschnitt " + chapterIndex++ + " (" + href + ") ===");

                    for (Element el : chapterDoc.select("h1, h2, h3, p")) {
                        String text = el.text().trim();
                        if (text.isEmpty()) continue;

                        if (el.tagName().startsWith("h")) {
                        	printStream.println("\n# " + text);
                        } else {
                        	printStream.println(text);
                        }
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

    private static String getOpfPath(ZipFile zipFile) throws Exception {
        Document containerDoc = parseXmlFromZip(zipFile, "META-INF/container.xml");
        Element rootfile = containerDoc.selectFirst("rootfile[full-path]");
        if (rootfile == null) {
            throw new IllegalStateException("Kein rootfile in container.xml gefunden.");
        }
        return rootfile.attr("full-path");
    }

    private static Document parseXmlFromZip(ZipFile zipFile, String entryPath) throws Exception {
        ZipEntry entry = zipFile.getEntry(entryPath);
        if (entry == null) {
            throw new IllegalStateException("Eintrag nicht im EPUB gefunden: " + entryPath);
        }
        try (InputStream is = zipFile.getInputStream(entry)) {
            return Jsoup.parse(is, StandardCharsets.UTF_8.name(), "", Parser.xmlParser());
        }
    }
}
