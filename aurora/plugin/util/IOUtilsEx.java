package aurora.plugin.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import uncertain.util.FastStringReader;

/**
 * extends org.apache.commons.io.IOUtils
 * 
 * @author jessen
 *
 */
public class IOUtilsEx extends IOUtils {

	public static InputStream newInputStream(String filePath)
			throws FileNotFoundException {
		return new FileInputStream(filePath);
	}

	public static BufferedReader newBufferedReader(String filePath,
			String encoding) throws FileNotFoundException,
			UnsupportedEncodingException {
		return newBufferedReader(newInputStream(filePath), encoding);
	}

	public static BufferedReader newBufferedReader(InputStream is,
			String encoding) throws UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(is, encoding));
	}

	public static ArrayList<String> readAllLines(String filePath,
			String encoding) throws IOException {
		return readAllLines(newInputStream(filePath), encoding);
	}

	/**
	 * is will be close after read
	 * 
	 * @param is
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readAllLines(InputStream is, String encoding)
			throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		String line = null;
		BufferedReader br = newBufferedReader(is, encoding);
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		return lines;
	}

	/**
	 * read inputstream as string
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String newString(InputStream in, String encoding)
			throws IOException {
		byte[] data = readAllBytes(in);
		return new String(data, encoding);
	}

	/**
	 * use StringBuilder to construct a string<br>
	 * StringBuilder is faster than StringBuffer<br>
	 * reader will be close after read
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String newString(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] cb = newCharArray();
		int len = -1;
		while ((len = reader.read(cb)) != -1)
			sb.append(cb, 0, len);
		reader.close();
		return sb.toString();
	}

	public static InputStream asInputStream(String source, String encoding)
			throws UnsupportedEncodingException {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				source.getBytes(encoding));
		return bais;
	}

	/**
	 * use uncertain.util.FastStringReader, it is faster than StringReader
	 * 
	 * @param source
	 * @return
	 */
	public static Reader asReader(String source) {
		return new FastStringReader(source);
	}

	/**
	 * default size 1024*8
	 * 
	 * @return
	 */
	public static byte[] newByteArray() {
		return new byte[1024 * 8];
	}

	public static byte[] newByteArray(int size) {
		return new byte[size];
	}

	/**
	 * default size 1024
	 * 
	 * @return
	 */
	public static char[] newCharArray() {
		return new char[1024];
	}

	public static char[] newCharArray(int size) {
		return new char[size];
	}

	/**
	 * is will be closed after read
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] readAllBytes(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		transfer(is, baos);
		return baos.toByteArray();
	}

	public static OutputStream newOutputStream(String filePath)
			throws FileNotFoundException {
		return new FileOutputStream(filePath);
	}

	public static BufferedWriter newBufferedWriter(String filePath,
			String encoding) throws UnsupportedEncodingException, IOException {
		return newBufferedWriter(newOutputStream(filePath), encoding);
	}

	public static BufferedWriter newBufferedWriter(OutputStream os,
			String encoding) throws UnsupportedEncodingException {
		return new BufferedWriter(new OutputStreamWriter(os, encoding));
	}

	public static void writeFile(InputStream is, String filePath)
			throws IOException {
		OutputStream os = newOutputStream(filePath);
		try {
			transfer(is, os);
		} finally {
			closeQuietly(os);
		}
	}

	public static void writeFile(Reader reader, String filePath, String encoding)
			throws IOException {
		BufferedWriter bw = newBufferedWriter(filePath, encoding);
		try {
			transfer(reader, bw);
		} finally {
			closeQuietly(bw);
		}
	}

	/**
	 * transfer all bytes from {@code in} to {@code out}<br>
	 * {@code in} will be close after transfer complete<br>
	 * 
	 * @param in
	 * @param out
	 * @return size of bytes
	 * @throws IOException
	 */
	public static int transfer(InputStream in, OutputStream out)
			throws IOException {
		byte[] buff = newByteArray();
		int size = 0;
		for (int len; (len = in.read(buff)) != -1; size += len)
			out.write(buff, 0, len);
		in.close();
		return size;
	}

	/**
	 * transfer chars from reader to writer<br>
	 * reader will be closed after transfer
	 * 
	 * @param reader
	 * @param writer
	 * @return size of chars
	 * @throws IOException
	 */
	public static int transfer(Reader reader, Writer writer) throws IOException {
		char[] cb = newCharArray();
		int size = 0;
		for (int len; (len = reader.read(cb)) != -1; size += len)
			writer.write(cb, 0, len);
		reader.close();
		return size;
	}

	public static void copyFile(String fileSrc, String fileDesc)
			throws IOException {
		OutputStream os = newOutputStream(fileDesc);
		try {
			transfer(newInputStream(fileSrc), os);
		} finally {
			closeQuietly(os);
		}
	}

}
