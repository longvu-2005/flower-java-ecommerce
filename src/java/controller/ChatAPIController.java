package controller;

import dao.MessageDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ChatAPIController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        String action = request.getParameter("action");
        MessageDao dao = new MessageDao();
        PrintWriter out = response.getWriter();

        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        String viewerRole = (userSession != null && userSession.getRoleId() == 1) ? "admin" : "user";

        if ("getUsers".equals(action)) {
            List<Map<String, String>> users = dao.getChattedCustomers();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                json.append("{\"clientId\":\"").append(users.get(i).get("clientId"))
                    .append("\",\"email\":\"").append(users.get(i).get("email")).append("\"}");
                if (i < users.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        } else if ("getMessages".equals(action)) {
            int clientId = 0;
            try {
                clientId = Integer.parseInt(request.getParameter("clientId"));
            } catch (NumberFormatException e) {
                out.print("[]"); return; 
            }

            if ("admin".equals(viewerRole)) {
                dao.markAsReadByAdmin(clientId);
            } else if ("user".equals(viewerRole)) {
                dao.markAsReadByUser(clientId);
            }
            List<Map<String, String>> msgs = dao.getMessagesByClient(clientId);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < msgs.size(); i++) {
                json.append("{\"role\":\"").append(msgs.get(i).get("role"))
                    .append("\",\"text\":\"").append(msgs.get(i).get("text").replace("\"", "\\\"").replace("\n", "\\n")).append("\"}");
                if (i < msgs.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
    }

    // Nhận tin nhắn mới và lưu xuống SQL
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        int clientId = Integer.parseInt(request.getParameter("clientId"));
        String clientEmail = request.getParameter("clientEmail");
        String senderRole = request.getParameter("senderRole");
        String messageText = request.getParameter("messageText");

        MessageDao dao = new MessageDao();
        dao.saveMessage(clientId, clientEmail, senderRole, messageText);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}