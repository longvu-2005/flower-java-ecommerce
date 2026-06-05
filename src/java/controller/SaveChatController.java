package controller;

import dao.MessageDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SaveChatController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Đảm bảo nhận Tiếng Việt có dấu không bị lỗi font
        request.setCharacterEncoding("UTF-8"); 
        
        String senderName = request.getParameter("senderName");
        String senderRole = request.getParameter("senderRole");
        String messageText = request.getParameter("messageText");

        if (senderName != null && senderRole != null && messageText != null) {
            MessageDao dao = new MessageDao();
            dao.saveMessage(0, senderRole, senderRole, messageText);
        }
        
        response.setStatus(HttpServletResponse.SC_OK); // Trả mã 200 báo lưu thành công
    }
}