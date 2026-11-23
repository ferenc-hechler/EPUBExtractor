package de.hechler.ai.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PDFExtractorMain {

	private final static String DEFAULT_PDF_PATH = "input/PDFs";
	private final static String DEFAULT_EXTRACT_PATH = "output/PDFs";
//	private final static String DEFAULT_PDF_PATH = "C:/EBOOKS";
//	private final static String DEFAULT_EXTRACT_PATH = "output/PDFs/EBOOKS";

	public static void main(String[] args) {
		String pdfPath = DEFAULT_PDF_PATH;
		String extractPath = DEFAULT_EXTRACT_PATH;
		switch (args.length) {
		case 0:
			break;
		case 1:
			pdfPath = args[0];
			extractPath = pdfPath;
			break;
		case 2:
			pdfPath = args[0];
			extractPath = args[0];
			break;
		default:
			System.err.println("Too many arguments provided. Usage: java PDFExtractorMain [pdfPath] [extractPath]");
			return;
		}

		System.out.println("pdfExtractor " + pdfPath + " -> " + extractPath);
		
		convertPdfToStructuredText(Paths.get(pdfPath), Paths.get(extractPath));
		
	}

	private static void convertPdfToStructuredText(Path pdfPath, Path extractPath) {
		if (Files.isRegularFile(pdfPath)) {
			convertFile(pdfPath, extractPath);
		}
		else if (Files.isDirectory(pdfPath)) {
			try {
				Files.walk(pdfPath)
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
					.forEach(path -> convertFile(path, pdfPath, extractPath));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return;
		}
		else {
			System.err.println("Invalid path: " + pdfPath);
			return;
		}
	}

	private static void convertFile(Path pdfPath, Path basePath, Path extractPath) {
		Path relativePath = basePath.relativize(pdfPath);
		Path targetPath = extractPath.resolve(relativePath);
		try {
			Files.createDirectories(targetPath.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		convertFile(pdfPath, targetPath);
	}

	private static void convertFile(Path pdfPath, Path extractPath) {
		if (extractPath.getFileName().toString().toLowerCase().endsWith(".pdf")) {
			extractPath = extractPath.resolveSibling(extractPath.getFileName().toString().replaceAll("(?i)\\.pdf$", ".txt"));
		}
		PdfTextExtractor extractor = new PdfTextExtractor();
		System.out.println("Extracting " + pdfPath + " to " + extractPath);
		extractor.extract(pdfPath, extractPath);
	}

}
