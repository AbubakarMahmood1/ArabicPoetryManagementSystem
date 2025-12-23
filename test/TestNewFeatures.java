import com.arabicpoetry.bll.service.VerseSimilarityService;
import com.arabicpoetry.bll.service.FrequencyService;
import com.arabicpoetry.model.linguistics.VerseSimilarity;
import com.arabicpoetry.model.linguistics.FrequencyEntry;
import com.arabicpoetry.model.linguistics.IndexEntry;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Test the three new features:
 * 1. N-gram similarity search
 * 2. Frequency calculation at poem/book levels
 * 3. Index generation for books
 */
public class TestNewFeatures {
    public static void main(String[] args) {
        try {
            testNGramSimilarity();
            testFrequencyCalculation();
            testIndexGeneration();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testNGramSimilarity() throws SQLException {
        System.out.println("=== Testing N-gram Similarity ===");
        VerseSimilarityService service = VerseSimilarityService.getInstance();

        // Test with a sample verse (replace with actual verse from your DB)
        String testVerse = "يا أيها الذين آمنوا اتقوا الله";
        double threshold = 0.3;

        List<VerseSimilarity> results = service.findSimilar(testVerse, threshold);

        System.out.println("Query: " + testVerse);
        System.out.println("Threshold: " + threshold);
        System.out.println("N-gram size: " + service.getNGramSize());
        System.out.println("Similar verses found: " + results.size());

        for (int i = 0; i < Math.min(5, results.size()); i++) {
            VerseSimilarity sim = results.get(i);
            System.out.printf("  [%.3f] Verse %d: %s%n",
                sim.getSimilarityScore(),
                sim.getVerse().getVerseId(),
                sim.getVerse().getText().substring(0, Math.min(50, sim.getVerse().getText().length())));
        }
        System.out.println();
    }

    private static void testFrequencyCalculation() throws SQLException {
        System.out.println("=== Testing Frequency Calculation ===");
        FrequencyService service = FrequencyService.getInstance();

        // Test with poemId=1 (replace with actual poem ID from your DB)
        int testPoemId = 1;

        System.out.println("Poem ID: " + testPoemId);

        List<FrequencyEntry> tokenFreqs = service.getTokenFrequenciesByPoem(testPoemId);
        System.out.println("Token frequencies (top 5):");
        for (int i = 0; i < Math.min(5, tokenFreqs.size()); i++) {
            FrequencyEntry entry = tokenFreqs.get(i);
            System.out.printf("  %s: %d%n", entry.getTerm(), entry.getCount());
        }

        List<FrequencyEntry> lemmaFreqs = service.getLemmaFrequenciesByPoem(testPoemId);
        System.out.println("Lemma frequencies (top 5):");
        for (int i = 0; i < Math.min(5, lemmaFreqs.size()); i++) {
            FrequencyEntry entry = lemmaFreqs.get(i);
            System.out.printf("  %s: %d%n", entry.getTerm(), entry.getCount());
        }

        List<FrequencyEntry> rootFreqs = service.getRootFrequenciesByPoem(testPoemId);
        System.out.println("Root frequencies (top 5):");
        for (int i = 0; i < Math.min(5, rootFreqs.size()); i++) {
            FrequencyEntry entry = rootFreqs.get(i);
            System.out.printf("  %s: %d%n", entry.getTerm(), entry.getCount());
        }
        System.out.println();
    }

    private static void testIndexGeneration() throws SQLException {
        System.out.println("=== Testing Index Generation ===");
        FrequencyService service = FrequencyService.getInstance();

        // Test with bookId=1 (replace with actual book ID from your DB)
        int testBookId = 1;

        System.out.println("Book ID: " + testBookId);

        Map<String, List<IndexEntry>> tokenIndex = service.generateTokenIndexByBook(testBookId);
        System.out.println("Token index entries: " + tokenIndex.size());

        Map<String, List<IndexEntry>> lemmaIndex = service.generateLemmaIndexByBook(testBookId);
        System.out.println("Lemma index entries: " + lemmaIndex.size());

        Map<String, List<IndexEntry>> rootIndex = service.generateRootIndexByBook(testBookId);
        System.out.println("Root index entries: " + rootIndex.size());

        // Show sample from token index
        String sampleTerm = tokenIndex.keySet().stream().findFirst().orElse(null);
        if (sampleTerm != null) {
            List<IndexEntry> entries = tokenIndex.get(sampleTerm);
            System.out.printf("%nSample token '%s' appears in %d locations:%n", sampleTerm, entries.size());
            for (int i = 0; i < Math.min(3, entries.size()); i++) {
                IndexEntry entry = entries.get(i);
                System.out.printf("  Verse %d (Poem %d), position %d%n",
                    entry.getVerseId(),
                    entry.getPoemId(),
                    entry.getPosition());
            }
        }
    }
}
