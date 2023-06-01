import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> wordsOnPage = new HashMap<>();// Результирующая мапа
    private static Set<String> stopList = new HashSet<>();
    private final File stopWords = new File("stop-ru.txt");

    private void readStopList() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(stopWords))) {
            String line = br.readLine();
            while (line != null) {
                stopList.add(line);
                line = br.readLine();
            }
        }
    }

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        readStopList();
        File[] pdfs = pdfsDir.listFiles();
        for (var pdf : pdfs) {
            var doc = new PdfDocument(new PdfReader(pdf));
            int pages = doc.getNumberOfPages();
            for (int i = 1; i < pages; i++) {
                String text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                String[] words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();

                for (String word : words) {
                    if ((word.isEmpty()) || (stopList.contains(word.toLowerCase()))) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);// Собираем промежуточную мапу из слов и их количества в текущей странице
                }

                for (String key : freqs.keySet()) {// Пробегаемся по всем ключам(СЛОВАМ) закинутым в промежуточную мапу Текущей СТРАНИЦЫ
                    int count = freqs.get(key);// получаем count для слова из значения пары промежуточной мапы
                    PageEntry pageEntry = new PageEntry(pdf.getName(), i, count);//Собираем pageEntry: закидываем этот count вместе с номером Текущей СТРАНИЦЫ и именем текущего файла в pageEntry
                    if (wordsOnPage.containsKey(key)) {//если в результирующей мапе такой ключь(СЛОВО) встречается
                        wordsOnPage.get(key).add(pageEntry);// то достаем по этому ключу(СЛОВУ) список List<pageEntry> и добавляем в него текущий pageEntry
                    } else {//если в результирующей мапе такой ключь(СЛОВО) НЕ встречается
                        List<PageEntry> list = new ArrayList<>();//создаем новый список List<pageEntry>
                        list.add(pageEntry);// добавляем в него текущий pageEntry
                        wordsOnPage.put(key, list);//  и кладем в результирующую мапу под новым ключем(СЛОВОМ) новый List<pageEntry>
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        List<PageEntry> respond = new ArrayList<>();
            if (wordsOnPage.containsKey(word.toLowerCase())) {//если такой ключ встречается
                respond.addAll(wordsOnPage.get(word.toLowerCase()));//добавляем в ответ все что достали из результирующей по ключу
            }
        Collections.sort(respond);//сортирум в соответствии с compareTo
        return respond;
    }
}

