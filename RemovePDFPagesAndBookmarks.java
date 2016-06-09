import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.SimpleBookmark;

/**
 * Remove Table of Contents (TOC), its related bookmarks and page labels from pdf using itext-2.1.7.
 */
public class iText {
	private static String outputFile = "output.pdf";
	private static String inputFile = "input.pdf";

	// @formatter:off

	/**
	 * Replace roman or other page labels with numeric.
	 * @param PdfStamper
	 */
	public static void updatePageLabels(PdfStamper stamper) {
		PdfDictionary root = stamper.getReader().getCatalog();
		PdfDictionary pageLabels = root.getAsDict(PdfName.PAGELABELS);
		if (pageLabels != null) {
			PdfArray newNums = new PdfArray();
			PdfArray nums = pageLabels.getAsArray(PdfName.NUMS);	
			if (nums != null) {
				for (int i = 0; i < nums.size() - 1;) {
					 nums.getAsNumber(i++).intValue();
					newNums.add(nums.getPdfObject(i++));
				}
			}
		}
		pageLabels.put(PdfName.NUMS, newNums);		
	}

	/**
	 * Search first word before space, as TOC next page no string contains other
	 * meta-information
	 * @param String
	 * @return returns first word before space from the input string
	 */
	private static String getFirstWord(String text) {
		if (text.indexOf(' ') > -1) { 						  // check if there is more than one word.
			return text.substring(0, text.indexOf(' '));	 // extract first word.
		} else {
			return text; 		  							// text is the first word itself.
		}
	}

	/**
	 * Removes TOC pages from a document.
	 * 
	 * @param PdfStamper
	 * @param int 
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 */
	public static void removePages(PdfStamper stamper, int pageNoNextToToc)
			throws DocumentException, FileNotFoundException {
		int i;
		PdfReader reader = stamper.getReader();
		int totalPages = reader.getNumberOfPages();
		List<Integer> newPagesList = new ArrayList<Integer>(totalPages);
		// populate newList with old pdf pages
		for (i = 1; i <= totalPages; i++) {
			newPagesList.add(i);
		}
		// remove TOC pages from new list		
		for (i = 0; i < (pageNoNextToToc - 1); i++) {
			newPagesList.remove(0);
		}	
		stamper.getReader().selectPages(newPagesList);									// list contains all pages except TOC pages
	}

	/**
	 * Code to remove TOC bookmark.
	 * 
	 * @param String 
	 * @param PdfStamper
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void removeTOCBookmark(String removeBookmark,
			PdfStamper stamper) throws DocumentException, IOException {
		List<HashMap> bookmarksList = new ArrayList<HashMap>();
		List<HashMap> bookmarks = SimpleBookmark.getBookmark(stamper.getReader());
		// add all other bookmarks in the new list except TOC bookmarks
		for (int i = 0; i < bookmarks.size(); i++) {								
			String title = ((String) bookmarks.get(i).get("Title"));				// title = "Table of Contents"
			if (!title.equals(removeBookmark))										
				bookmarksList.add(bookmarks.get(i));
		}	
		stamper.setOutlines(bookmarksList);											// add new list of bookmarks (except TOC) in the output pdf
		updatePageLabels(stamper);													// replace roman or other page labels with numeric.
		stamper.close();
	}

	public static void main(String[] args) throws Exception {
		String removeBookmark = "Table of Contents";
		PdfStamper stamper = new PdfStamper(new PdfReader(inputFile),new FileOutputStream(outputFile));
		PdfReader reader = new PdfReader(inputFile);																			
		List<HashMap<Object,Object>> bookmarks = SimpleBookmark.getBookmark(reader);					// read input file and store its bookmarks in a list
		String pageNoNextToTocString = (String) bookmarks.get(1).get("Page");			// extract page no next to TOC	
		int pageNoNextToToc = Integer.parseInt(getFirstWord(pageNoNextToTocString));	// search first word before space, as TOC next page no string contains other meta-information
		removePages(stamper, pageNoNextToToc);											// remove all TOC pages
		removeTOCBookmark(removeBookmark, stamper);								// remove TOC bookmark

	}
	// @formatter:on

}
