import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AnkiFlashcardExtractor {
    private static final String ANKI_BEGIN = "1. ANKI";
    private static final String ANKI_FRONT_INDICATOR = "1. Front";
    private static final String ANKI_BACK_INDICATOR = "2. Back";
    private static final String ANKI_END = "2. END ANKI";
    private static final String OUTPUT_FILE = "anki.csv";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AnkiFlashcardExtractor <markdown_file_path>");
            System.exit(1);
        }

        String markdownFilePath = args[0];
        List<String[]> flashcards = extractFlashcards(markdownFilePath);
        writeToCSV(flashcards);
    }

    private static List<String[]> extractFlashcards(String filePath) {
        List<String[]> flashcards = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder frontContent = new StringBuilder();
            StringBuilder backContent = new StringBuilder();
            boolean isFrontContent = false;
            boolean isBackContent = false;
            boolean nowCard = false;

            while ((line = reader.readLine()) != null) {
                if (nowCard) {
                    if (line.trim().equals(ANKI_FRONT_INDICATOR)) {
                        isFrontContent = true;
                        isBackContent = false;
                    } else if (line.trim().equals(ANKI_BACK_INDICATOR)) {
                        isFrontContent = false;
                        isBackContent = true;
                    } else if (line.trim().equals(ANKI_END)) {
                        if (frontContent.length() > 0 && backContent.length() > 0) {
                            String[] flashcard = new String[]{frontContent.toString().trim(), backContent.toString().trim()};
                            flashcards.add(flashcard);
                            frontContent.setLength(0);
                            backContent.setLength(0);
                        }
                        isFrontContent = false;
                        isBackContent = false;
                    } else if (isFrontContent) {
                        frontContent.append(line.trim());
                    } else if (isBackContent) {
                        backContent.append(line.trim());
                    }
                } else if (line.trim().equals(ANKI_BEGIN)) {
                    nowCard = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flashcards;
    }

    private static void writeToCSV(List<String[]> flashcards) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            for (String[] flashcard : flashcards) {
                String frontContent = escapeCSV(flashcard[0]);
                String backContent = escapeCSV(flashcard[1]);
                writer.write(frontContent + ";" + backContent);
                writer.newLine();
            }
            System.out.println("Flashcards extracted and saved to " + OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeCSV(String content) {
        return "\"" + content.replaceAll("\"", "\"\"") + "\"";
    }
}