package aurora.plugin.bgtcheck.dataimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveNotSQLLine {

	private LinkedList<String> illegalLineList = new LinkedList<String>();
	private LinkedList<String> illegalCommandList = new LinkedList<String>();
	private String fileCharsetName = "GBK";

	public void execute(String sqlDirPath) throws IOException {
		File sqlFileDir = new File(sqlDirPath);
		execute(sqlFileDir);
	}

	public void execute(File sqlFileDir) throws IOException {
		if (!sqlFileDir.exists()) {
			throw new IllegalArgumentException("dir:" + sqlFileDir + " is not exists!");
		}
		File[] files = sqlFileDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				execute(file);
			} else {
				if (file.getName().toLowerCase().endsWith(".sql"))
					handleSQLFile(file.getCanonicalPath());
			}
		}
	}

	private void init() {
		illegalLineList.add("set feedback off");
		illegalLineList.add("set define off");
		illegalCommandList.add("prompt");
	}

	public void handleSQLFile(String file) throws IOException {
		BufferedReader input = null;
		PrintWriter output = null;
		File inFile = new File(file);
		File outFile = new File(file + ".temp");
		File correctFile = null;
		boolean hasIlleageLine = false;
		try {
			InputStream is = new FileInputStream(inFile);
			InputStreamReader isr = new InputStreamReader(is, fileCharsetName);
			input = new BufferedReader(isr);
			output = new PrintWriter(new FileWriter(outFile));
			String line = null;
			boolean isIlleageLine = false;
			boolean hasCheckName = false;
			String sqlCommand = "insert into";
			while ((line = input.readLine()) != null) {
				if (!hasCheckName) {
					if (line.startsWith(sqlCommand)) {
						int tableNameEndIndex = line.indexOf("(");
						String realTableName = line.substring(sqlCommand.length(), tableNameEndIndex).trim();
						correctFile = getcorrectSQLFile(inFile, realTableName);
						hasCheckName = true;
					}
				}

				if (illegalLineList.contains(line)) {
					isIlleageLine = true;
				}
				for (String illegalCommand : illegalCommandList) {
					if (line.startsWith(illegalCommand)) {
						isIlleageLine = true;
					}
				}
				if (!isIlleageLine) {
					output.println(line);
				} else {
					isIlleageLine = false;
					hasIlleageLine = true;
				}
			}
			input.close();
			output.flush();
			output.close();
			if (hasIlleageLine) {
				if (inFile.delete()) {
					if (correctFile != null) {
						outFile.renameTo(correctFile);
					} else
						outFile.renameTo(new File(file));
				}
			} else {
				if (correctFile != null && !correctFile.getCanonicalPath().equalsIgnoreCase(inFile.getCanonicalPath())) {
					inFile.renameTo(correctFile);
				}
				outFile.delete();
			}
		} finally {
			closeReader(input);
			closeWriter(output);
		}
	}

	private void closeReader(Reader reader) {
		if (reader != null)
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void closeWriter(Writer writer) {
		if (writer != null)
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private File getcorrectSQLFile(File file, String realTableName) throws IOException {
		String oldFileName = file.getName();
		String fileNameWithoutExtension = oldFileName.substring(0, oldFileName.lastIndexOf("."));
		String fileNameExtension = oldFileName.substring(oldFileName.lastIndexOf("."));

		Pattern pattern = Pattern.compile("([\\w]*)(.*)");
		Matcher matcher = pattern.matcher(fileNameWithoutExtension);
		String tableName = "";
		String fileDesc = "";

		if (matcher.find()) {
			tableName = matcher.group(1);
			fileDesc = matcher.group(2);
		}
		if (tableName.trim().equals(realTableName))
			return file;
		String newFileName = realTableName + fileDesc + fileNameExtension;
		File newFile = new File(file.getParent(), newFileName);
		System.out.println("new FileName. file:" + file.getCanonicalPath() + " new FileName:" + newFileName);
		return newFile;
	}

	public static void main(String[] args) throws IOException {
		String sqlFileRootDir = "E:\\workspace\\worksheet\\bgt_check_with_cache\\预算检查";
		RemoveNotSQLLine rns = new RemoveNotSQLLine();
		rns.init();
		rns.execute(sqlFileRootDir);
		System.out.println("done.");
	}
}
