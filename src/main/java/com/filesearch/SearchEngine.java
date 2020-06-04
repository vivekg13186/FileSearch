package com.filesearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchEngine implements FileReadListener {

    private final String CONTENT="content";
    private final String FILE_NAME="filename";
    private String folderPath;
    private Path indexPath;
    private int fileCount;
    private final IndexWriter writer;
    private int totalCount;
    private Analyzer analyzer;
    private Directory memoryIndex;
    private boolean ready = false;
    private TikaConfig config;
    private SearchListener listener;


    public static Query createPhraseQuery(String[] phraseWords, String field) {
        SpanQuery[] queryParts = new SpanQuery[phraseWords.length];
        for (int i = 0; i < phraseWords.length; i++) {
            String w =phraseWords[i];
            System.out.println(w);
            if(w.endsWith("*")){
                System.out.println(w.substring(0,w.length()-1));
                PrefixQuery pq = new PrefixQuery(new Term(field, w.substring(0,w.length()-1)));
                //WildcardQuery wildQuery = new WildcardQuery(new Term(field, phraseWords[i]));
                //queryParts[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildQuery);
                queryParts[i] = new SpanMultiTermQueryWrapper<PrefixQuery>(pq);
            }else{
                queryParts[i] =new SpanTermQuery(new Term(field, w));
            }

        }
        return new SpanNearQuery(queryParts,       //words
                0,                //max distance
                true              //exact order
        );
    }

    private TikaConfig getConfig(boolean disableOCR) throws TikaException, IOException, SAXException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<properties>\n" +
                "    <parsers>\n" +
                "        <parser class=\"org.apache.tika.parser.DefaultParser\">\n" +
                "       <parser-exclude class=\"org.apache.tika.parser.ocr.TesseractOCRParser\"/>\n" +
                "        </parser>\n" +
                "    </parsers>\n" +
                "</properties>";
        if(false){
            return  new TikaConfig(new ByteArrayInputStream(xml.getBytes()));
        }else{
            return  new TikaConfig();
        }
    }

    public SearchEngine(String folder,boolean disableOCR) throws IOException, TikaException, SAXException {
        folderPath = folder;
        indexPath = Files.createTempDirectory("FileSearch");
        memoryIndex = new SimpleFSDirectory(indexPath);
        analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(memoryIndex, indexWriterConfig);
        config =getConfig(disableOCR);
    }



    private void indexFile(File file, SearchListener listener) {
            this.listener=listener;
            FileContentReader.read(file,this);
    }

    public List<SearchResult> search(String text, SearchListener listener) {
        try {
            List<Document> docs = searchIndex(text);
            ArrayList<SearchResult> result = new ArrayList<>(docs.size());
            for (Document d : docs) {
                SearchResult r = new SearchResult();
                r.filename = d.get(FILE_NAME);
                result.add(r);
            }
            return result;
        } catch (ParseException e) {
            listener.error(e.getMessage());
        } catch (IOException e) {
            listener.error(e.getMessage());
        }
        return null;
    }

    private List<Document> searchIndex(String queryString) throws ParseException, IOException {
        QueryParser parser=new QueryParser(CONTENT,analyzer);
        System.out.println(queryString);
        Query query = parser.parse(queryString);
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 100000000);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }

        return documents;
    }

    public void startIndexing(SearchListener listener) {
        try {
            List<File> files = getAllFiles();
            totalCount = files.size();
            for (File file : files) {
                fileCount++;
                indexFile(file, listener);
            }
            writer.close();
            listener.setProgress(totalCount, fileCount, "Index complete ready for search");
            ready=true;
        } catch (IOException e) {
            listener.setProgress(totalCount, 0, e.getMessage());
        }
    }

    private List<File> getAllFiles() throws IOException {
        return Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    public boolean readyForSearch() {
        return ready;
    }

    @Override
    public void fileRead(String content, String file) {
        Document document = new Document();
        document.add(new TextField(FILE_NAME, file, Field.Store.YES));
        document.add(new TextField(CONTENT, content, Field.Store.YES));
        listener.setProgress(totalCount, fileCount, "File added to index : " + file);
        try {
            writer.addDocument(document);
        } catch (IOException e) {
            listener.setProgress(totalCount, fileCount, e.getMessage());
        }


    }
}
