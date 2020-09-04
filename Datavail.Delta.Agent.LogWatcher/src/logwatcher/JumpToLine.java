package logwatcher;

import static logwatcher.DefaultCharacterEncoding.UTF_8;
import static org.apache.commons.io.IOUtils.lineIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
/**
 * @File_Desc: Log watcher info Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :JumpToLine
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public final class JumpToLine {

	private final InputStream is_;
	private final String charsetName_;

	private final LineIterator it_;

	private long lastLineRead_ = -1L;

	/**
	 * Opens any underlying streams/readers and immeadietly seeks to the line in
	 * the file that's next to be read; skipping over lines in the file this
	 * reader has already read.
	 */
	public JumpToLine(final InputStream is, final String charsetName) throws IOException {
		is_ = is;
		charsetName_ = charsetName;
		lastLineRead_ = 1L;
		try {
			it_ = lineIterator(is_, charsetName_);
		} catch (IOException e) {
			close();
			throw e;
		}
	}

	public JumpToLine(final InputStream is) throws IOException {
		this(is, UTF_8);
	}

	public JumpToLine(final File file, final String charsetName) throws IOException {
		this(new FileInputStream(file), charsetName);
	}

	public JumpToLine(final File file) throws IOException {
		this(file, UTF_8);
	}

	/**
	 * Seeks to the last line read in the file.
	 */
	public long seek() {
		return seek(lastLineRead_);
	}

	/**
	 * Seeks to a given line number in the stream/file.
	 * 
	 * @param line
	 *            the line number to seek to
	 */
	public long seek(final long line) {
		long lineCount = 1L;
		while ((it_ != null) && (it_.hasNext()) && (lineCount < line)) {
			it_.nextLine();
			lineCount += 1L;
		}
		// If we got to the end of the file, but haven't read as many
		// lines as we should have, then the requested line number is
		// out of range.
		if (lineCount < line) {
			throw new NoSuchElementException("Invalid line number; " + "out of range.");
		}
		lastLineRead_ = lineCount;
		return lineCount;
	}

	/**
	 * Closes this IOUtils LineIterator and the underlying input stream reader.
	 */
	public void close() {
		IOUtils.closeQuietly(is_);
		LineIterator.closeQuietly(it_);
	}

	/**
	 * Returns true of there are any more lines to read in the file. Otherwise,
	 * returns false.
	 * 
	 * @return
	 */
	public boolean hasNext() {
		return it_.hasNext();
	}

	/**
	 * Read a line of text from this reader.
	 * 
	 * @return
	 */
	public String readLine() {
		String ret = null;
		try {
			// If there is nothing more to read with this LineIterator
			// then nextLine() throws a NoSuchElementException.
			ret = it_.nextLine();
			lastLineRead_ += 1L;
		} catch (NoSuchElementException e) {
			throw e;
		}
		return ret;
	}

	public long getLastLineRead() {
		return lastLineRead_;
	}

}
