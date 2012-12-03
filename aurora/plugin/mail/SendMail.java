package aurora.plugin.mail;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {

	private String ttitle;
	private String tcontent;
	private String smtpServer;
	private String tto;
	private String cto;
	private String tfrom;
	private String password;
	private String userName;
	private String port;
	private Attachment[] attachments;
	private boolean auth = false;

	public void check() {
		if (smtpServer == null || "".equals(smtpServer)) {
			throw new SendMailException("收件人地址不能为空");
		} else if (tfrom == null || "".equals(tfrom)) {
			throw new SendMailException("发件人不能为空不能为空");
		} else if (password == null || "".equals(password)) {
			throw new SendMailException("服务器密码不能为空");
		} else if (tcontent == null || "".equals(tcontent)) {
			throw new SendMailException("邮件内容不能为空");
		} else if (tto == null || "".equals(tto)) {
			throw new SendMailException("收件人地址不能为空");
		}
	}

	public void sendMail() throws Exception {
		// JavaMail需要Properties来创建一个session对象。它将寻找字符串"mail.smtp.host"，属性值就是发送邮件的主机.
		// Properties对象获取诸如邮件服务器、用户名、密码等信息，以及其他可在整个应用程序中 共享的信息。

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpServer);// 存储发送邮件服务器的信息
		props.put("mail.smtp.auth", "true");// 同时通过验证
		props.put("mail.smtp.port", port);

		Session s = null;
		if(auth){ //服务器需要身份认证   
			props.put("mail.smtp.auth","true");
			SmtpAuth smtpAuth=new SmtpAuth(userName,password);  
            s=Session.getDefaultInstance(props, smtpAuth);    
        }else{   
            props.put("mail.smtp.auth","false");   
            s=Session.getDefaultInstance(props, null);   
        }   
		
		// s.setDebug(true);// 设置调试标志,要查看经过邮件服务器邮件命令，可以用该方法
		// Message类表示单个邮件消息，它的属性包括类型，地址信息和所定义的目录结构。

		Message message = new MimeMessage(s);// 由邮件会话新建一个消息对象
		Address from = new InternetAddress(tfrom);// 发件人的邮件地址
		message.setFrom(from);// 设置发件人

		// Address to = new InternetAddress(tto);// 收件人的邮件地址
		message.addRecipients(Message.RecipientType.TO, InternetAddress
				.parse(tto));// 设置收件人,并设置其接收类型为TO,还有3种预定义类型如下：

		if (cto != null && !"".equals(cto)) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress
					.parse(cto));// 设置抄送
		}

		message.setSubject(ttitle);// 设置主题
		message.setSentDate(new Date());// 设置发信时间
		/*
		 * try { message.setDataHandler(new DataHandler(new String(message
		 * .getBytes("utf-8"), "utf-8"), "text/html;charset=utf-8")); } catch
		 * (UnsupportedEncodingException e) { e.printStackTrace(); }
		 */
		// message.setContent(tcontent, "text/html;charset=utf-8");

		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(tcontent, "text/html;charset=utf-8");
		mp.addBodyPart(mbp);
		addAttachment(mp);
		message.setContent(mp);
		Transport.send(message, message.getAllRecipients());
		
//		message.saveChanges();// 存储邮件信息
//		
//		// Transport 是用来发送信息的，
//		// 用于邮件的收发打操作。
//		Transport transport = null;
//		try {
//			transport = s.getTransport("smtp");
//			transport.connect(smtpServer, userName, password);// 以smtp方式登录邮箱
//			transport.sendMessage(message, message.getAllRecipients());// 发送邮件,其中第二个参数是所有已设好的收件人地址
//		} finally {
//			if (transport != null && transport.isConnected()) {
//				transport.close();
//			}
//		}
	}
	class SmtpAuth extends Authenticator {    
	    private String username,password;    
	   
	    public SmtpAuth(String username,String password){    
	        this.username = username;     
	        this.password = password;     
	    }    
	    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {    
	        return new javax.mail.PasswordAuthentication(username,password);    
	    }    
	} 
    protected void addAttachment(Multipart mp) throws MessagingException{
    	if(attachments != null){
    		MimeBodyPart mbp;
    		FileDataSource fds;
    		String fileName;
    		for(int i=0;i<attachments.length;i++){
    			 mbp=new MimeBodyPart();  
    			 fileName=attachments[i].getPath(); //选择出每一个附件名   
    			 fds =new FileDataSource(fileName); //得到数据源   
                 mbp.setDataHandler(new DataHandler(fds)); //得到附件本身并至入BodyPart   
                 mbp.setFileName(attachments[i].getName());  //得到文件名同样至入BodyPart   
                 mp.addBodyPart(mbp);   
    		}
        }    
    }

	public String getTtitle() {
		return ttitle;
	}

	public void setTtitle(String ttitle) {
		this.ttitle = ttitle;
	}

	public String getTcontent() {
		return tcontent;
	}

	public void setTcontent(String tcontent) {
		this.tcontent = tcontent;
	}

	public String getTfrom() {
		return tfrom;
	}

	public void setTfrom(String tfrom) {
		this.tfrom = tfrom;
	}

	public String getTto() {
		return tto;
	}

	public void setTto(String tto) {
		this.tto = tto;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getCto() {
		return cto;
	}

	public void setCto(String cto) {
		this.cto = cto;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setAttachments(Attachment[] attaches) {
		this.attachments = attaches;
	}
	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}
	
}
