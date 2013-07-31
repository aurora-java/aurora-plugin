package aurora.plugin.export.word;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class BBCodeParser {
	
//	public static void main(String[] args) throws Exception{
//		String text = "测试[100]一下[br]回车[b]粗体[u]下划线[/u]粗体[/b]结尾文字";//
//		BBCodeParser parser = new BBCodeParser();
//		parser.parse(text);
//	}
	
	public static final String TAG_BR = "[br]";
	public static final String TAG_B_S = "[b]";
	public static final String TAG_B_E = "[/b]";
	public static final String TAG_U_S = "[u]";
	public static final String TAG_U_E = "[/u]";
	
	
	private static final String[] DEFAULT_TAGS = new String[]{TAG_BR,TAG_B_S,TAG_B_E,TAG_U_S,TAG_U_E};
	
	private static final char TAG_START = '[';
	private static final char TAG_END = ']';
	
	public List<String> parse(String text) throws IOException{
		BBCodeHandler handler = new BBCodeHandler();
		parse(text,handler);
		return handler.getResult();
	}
	
	private void parse(String text, BBCodeHandler handler) throws IOException {
		Reader reader = new StringReader(text);
		String tag = "";
		int index=0,ts=0,te=0,ss=0;
		int ch;
		while ((ch = reader.read()) != -1) {
			char chr = (char) ch;
			if(chr == TAG_START) {
				ts = index;
			}else if(chr == TAG_END){
				te = index;
				tag = text.substring(ts,te+1);
				if(isTag(tag)){
					String t = text.substring(ss,ts);
					handler.process(t,tag);
					ss = te+1;
				}
			}
			index ++;
		}
		if(ss != index){
			handler.process(text.substring(ss),"");
		}
	}
	
	public boolean isTag(String t){
		for(int i=0;i<DEFAULT_TAGS.length;i++){
			String tag = DEFAULT_TAGS[i];
			if(tag.equalsIgnoreCase(t)){
				return true;
			}
		}
		return false;
	}
	
	class BBCodeHandler {
		private List<String> list = new ArrayList<String>();		
		public void process(String text,String tag){
			if(!"".equals(text)){
				list.add(text);
//				System.out.println("text: " + text);
			}
			if(!"".equals(tag)){
				list.add(tag);
//				System.out.println("tag: " + tag);
			}
			
		}
		
		public List<String> getResult(){
			return this.list;
		}
	}
}
