package aurora.plugin.poi;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * Utility to facilitate workaround when writing large OOXML spreadsheets with Apache POI. Taken
 * from existing example by Yegor Kozlov
 * 
 * @see <a
 *      href="http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/usermodel/examples/BigGridDemo.java">Code
 *      by Yegor Kozlov</a>
 * 
 */
public class BigGridUtil {
    /**
     * 
     * @param zipfile
     *            the template file
     * @param tmpfile
     *            the XML file with the sheet data
     * @param entry
     *            the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
     * @param out
     *            the stream to write the result to
     */
    public static void substitute(File zipfile, File tmpfile, String entry, OutputStream out)
            throws IOException {
        ZipFile zip = new ZipFile(zipfile);

        ZipOutputStream zos = new ZipOutputStream(out);

        @SuppressWarnings("unchecked")
        Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
        while (en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if (!ze.getName().equals(entry)) {
                zos.putNextEntry(new ZipEntry(ze.getName()));
                InputStream is = zip.getInputStream(ze);
                copyStream(is, zos);
                is.close();
            }
        }
        zos.putNextEntry(new ZipEntry(entry));
        InputStream is = new FileInputStream(tmpfile);
        copyStream(is, zos);
        is.close();
        zos.close();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >= 0) {
            out.write(chunk, 0, count);
            out.flush();
        }
    }   
    
    public static void sortList(List list){
    	if(list==null)return;
    	StringAscComparator comparator=new StringAscComparator();
    	Collections.sort(list, comparator);    	
    }   
}
class StringAscComparator implements Comparator{
	public int compare(Object arg0, Object arg1) {			
		return arg0.toString().compareTo(arg1.toString());
	}    	
}