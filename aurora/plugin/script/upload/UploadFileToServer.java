package aurora.plugin.script.upload;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import uncertain.composite.CompositeMap;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class UploadFileToServer {
	private CompositeMap context;
	private HttpServletRequest request;

	public UploadFileToServer(CompositeMap context) {
		this.context = context;
		// ServiceContext service =
		// ServiceContext.createServiceContext(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		this.request = serviceInstance.getRequest();
		assert request != null;
	}

	public void doUpload() {
		try {
			DiskFileItemFactory diskFactory = new DiskFileItemFactory();
			// threshold 极限、临界值，即硬盘缓存 1M
			diskFactory.setSizeThreshold(4 * 1024);
			// repository 贮藏室，即临时文件目录
			String string = context.getString("tempPath", "");
			if ("".equals(string) == false)
				diskFactory.setRepository(new File(string));

			ServletFileUpload upload = new ServletFileUpload(diskFactory);
			// 设置允许上传的最大文件大小 10M
			int max = context.getInt("maxSize", 10);
			upload.setSizeMax(max * 1024 * 1024);
			// 解析HTTP请求消息头
			List fileItems = upload.parseRequest(request);
			Iterator iter = fileItems.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					System.out.println("处理表单内容 ...");
//					processFormField(item);
				} else {
					System.out.println("处理上传的文件 ...");
					processUploadFile(item);
				}
			}// end while()

		} catch (Exception e) {
			System.out.println("使用 fileupload 包时发生异常 ...");
			e.printStackTrace();
		}// end try ... catch ...
	}// end doPost()

	// 处理表单内容
	private void processFormField(FileItem item) throws Exception {
		String name = item.getFieldName();
		String value = item.getString();
		System.out.println(name + " : " + value + "\r\n");
	}

	// 处理上传的文件
	private void processUploadFile(FileItem item) throws Exception {
		// 此时的文件名包含了完整的路径，得注意加工一下
		String filename = item.getName();
		System.out.println("完整的文件名：" + filename);
		int index = filename.lastIndexOf("\\");
		filename = filename.substring(index + 1, filename.length());

		long fileSize = item.getSize();

		if ("".equals(filename) && fileSize == 0) {
			System.out.println("文件名为空 ...");
			return;
		}
		String path = context.getString("filePath", "");
		File uploadFile = new File(path + "/" + filename);
		if(new File(path).exists() == false)
		FileUtils.forceMkdir(new File(path));
		if (uploadFile.exists() == false) {
			uploadFile.createNewFile();
			//
		}
		item.write(uploadFile);

		CompositeMap files = context.getChild("uploadFiles");
		if (files == null) {
			files = context.createChild("uploadFiles");
		}
		files.createChild("file").put("filePath", uploadFile.getPath());

		System.out.println(filename + " 文件保存完毕 ...");
		System.out.println("文件大小为 ：" + fileSize + "\r\n");
	}
	static public void doDelFile(String filePath){
		try {
			FileUtils.forceDelete(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
