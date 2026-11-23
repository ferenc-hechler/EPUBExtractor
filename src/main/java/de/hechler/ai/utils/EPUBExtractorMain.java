package de.hechler.ai.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EPUBExtractorMain {

	private final static String DEFAULT_EPUB_PATH = "input/EPUBs";
	private final static String DEFAULT_EXTRACT_PATH = "output/EPUBs";
//	private final static String DEFAULT_EPUB_PATH = "C:/EBOOKS";
//	private final static String DEFAULT_EXTRACT_PATH = "output/EPUBs/EBOOKS";

	public static void main(String[] args) {
		String epubPath = DEFAULT_EPUB_PATH;
		String extractPath = DEFAULT_EXTRACT_PATH;
		switch (args.length) {
		case 0:
			break;
		case 1:
			epubPath = args[0];
			extractPath = epubPath;
			break;
		case 2:
			epubPath = args[0];
			extractPath = args[0];
			break;
		default:
			System.err.println("Too many arguments provided. Usage: java EPUBExtractorMain [epubPath] [extractPath]");
			return;
		}

		System.out.println("EPUBExtractor " + epubPath + " -> " + extractPath);
		
		convertEpubToStructuredText(Paths.get(epubPath), Paths.get(extractPath));
		
	}

	private static void convertEpubToStructuredText(Path epubPath, Path extractPath) {
		if (Files.isRegularFile(epubPath)) {
			convertFile(epubPath, extractPath);
		}
		else if (Files.isDirectory(epubPath)) {
			try {
				Files.walk(epubPath)
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().toLowerCase().endsWith(".epub"))
					.forEach(path -> convertFile(path, epubPath, extractPath));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return;
		}
		else {
			System.err.println("Invalid path: " + epubPath);
			return;
		}
	}

	private static void convertFile(Path epubPath, Path basePath, Path extractPath) {
		Path relativePath = basePath.relativize(epubPath);
		Path targetPath = extractPath.resolve(relativePath);
		try {
			Files.createDirectories(targetPath.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		convertFile(epubPath, targetPath);
	}

	private static void convertFile(Path epubPath, Path extractPath) {
		if (extractPath.getFileName().toString().toLowerCase().endsWith(".epub")) {
			extractPath = extractPath.resolveSibling(extractPath.getFileName().toString().replaceAll("(?i)\\.epub$", ".txt"));
		}
		StructuredEpubExtractor extractor = new StructuredEpubExtractor();
		System.out.println("Extracting " + epubPath + " to " + extractPath);
		extractor.extract(epubPath, extractPath);
	}

}
