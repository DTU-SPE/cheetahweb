package org.cheetahplatform.web.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ModelServlet extends HttpServlet {

	private static final long serialVersionUID = -3135344089672089371L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String processInstance = request.getParameter("processInstance");
		String version = request.getParameter("version");

		String realPath = getServletContext().getRealPath("../cheetah/models/" + processInstance + "/" + version + ".png");
		FileInputStream stream = new FileInputStream(realPath);
		FileChannel channel = stream.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
		channel.read(buffer);
		response.getOutputStream().write(buffer.array());
		stream.close();
	}

}
