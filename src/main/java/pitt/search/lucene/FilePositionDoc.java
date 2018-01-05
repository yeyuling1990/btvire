package pitt.search.lucene;
import java.io.File;
import java.io.FileReader;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;

/**
 *  This class makes a minor modification to org.apache.lucene.FileDocument
 *  such that it records TermPositionVectors for each document
 *  @author Trevor Cohen
 */
public class FilePositionDoc  {

  public static Document Document(File f,String time)
       throws java.io.FileNotFoundException {
    Document doc = new Document();
    doc.add(new StoredField("pn", f.getPath()));
    doc.add(new Field("modified",time,
        Field.Store.YES, Field.Index.NOT_ANALYZED));
    //create new FieldType to store term positions (TextField is not sufficiently configurable)
    FieldType ft = new FieldType();
    ft.setIndexed(true);
    ft.setTokenized(true);
    ft.setStoreTermVectors(true);
    ft.setStoreTermVectorPositions(true);
    Field contentsField = new Field("abstract", new FileReader(f), ft);

    doc.add(contentsField);
    return doc;
  }
}
