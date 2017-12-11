package aurora.plugin.invoicecheck.util;

import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.Properties;

/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 基础工具类
 * @Time: 2017/10/17 14:02
 */
public class BaseUtil {

    private static final String JS_PACKAGE = "aurora/plugin/invoicecheck/js/";

    public static boolean GenerateImage(String base64Str, String path, String imageName) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 根据base64字符串生成验证码图片
         * @Time: 2017/10/17 14:03
         * @param base64Str base64字符串
         * @param path python脚本路径
         * @param imageName 图片名称，根据随机规则生成
         * @Return: 转换是否成功
         */
        if(base64Str.trim() == null || base64Str.trim().equals("")){//当base64字符串为空时返回false
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b = decoder.decodeBuffer(base64Str);
        for(int i=0;i<b.length;++i) {
            if(b[i]<0) {//调整异常数据
                b[i]+=256;
            }
        }
        String imgFilePath = path + imageName + ".png";
        OutputStream out = new FileOutputStream(imgFilePath);//生成验证码png图片
        out.write(b);
        out.flush();
        out.close();
        return true;
    }

    public static String getLocalJs(String jsName) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 根据js文件名获取本地js文件
         * @Time: 2017/10/17 14:16
         * @param jsName js文件名
         * @Return: js文件字符串
         */
        InputStream is = BaseUtil.class.getClassLoader().getResourceAsStream(JS_PACKAGE + jsName + ".js");
        return convertStreamToString(is);
    }

    public static String convertStreamToString(InputStream is) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 将输入流转为字符串
         * @Time: 2017/10/17 14:22
         * @param is 输入流
         * @Return: 输入流内容
         */
        BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while((line = reader.readLine()) != null) {
            sb.append(line + "\n");//不加换行符js在运行过程中可能会出错
        }
        is.close();
        return sb.toString();
    }

    public static String getPropertity(String key) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 读取properties文件
         * @Time: 2017/10/19 15:26
         * @param key 属性名
         * @Return: 属性值
         */
        String propertity = null;

            Properties prop = new Properties();
            InputStream in = BaseUtil.class.getResourceAsStream("/aurora/plugin/invoicecheck/config/invoiceCheck.properties");
            prop.load(in);
            propertity = prop.getProperty(key);
        return propertity;
    }
}
