package aurora.plugin.qrcode;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeServlet extends HttpServlet {

	/**
	 * @Fields serialVersionUID : serialVersionUID
	 */

	private static final long serialVersionUID = 1L;

	private static final String KEY = "keycode";
	private static final String SIZE = "msize";
	private static final String IMAGETYPE = "JPEG";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String keycode = req.getParameter(KEY);
		// QR的默认编码方式为ISO8859-1,进行一次编码转换
		keycode = new String(keycode.getBytes("UTF-8"), "ISO-8859-1");

		if (keycode != null && !"".equals(keycode)) {
			ServletOutputStream stream = null;
			try {
				int size = 129;
				String msize = req.getParameter(SIZE);
				if (msize != null && !"".equals(msize.trim())) {
					try {
						size = Integer.valueOf(msize);
					} catch (NumberFormatException e) {
						// TODO output to log
					}
				}
				stream = resp.getOutputStream();
				QRCodeWriter writer = new QRCodeWriter();
				BitMatrix m = writer.encode(keycode, BarcodeFormat.QR_CODE, size, size);
				MatrixToImageWriter.writeToStream(m, IMAGETYPE, stream);
			} catch (WriterException e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}

}