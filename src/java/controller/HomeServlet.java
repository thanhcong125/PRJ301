package controller;

import dao.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lọc theo type nếu có
        String type = request.getParameter("type");
        if (type != null && !type.trim().isEmpty()) {
            type = type.trim();

            List<Room> roomsByType = roomDAO.searchRoomsByType(type);
            if (roomsByType != null && !roomsByType.isEmpty()) {
                response.setContentType("text/html;charset=UTF-8");
                for (Room room : roomsByType) {
                    String cityName = (room.getPropertyId() != null && room.getPropertyId().getCity() != null)
                            ? room.getPropertyId().getCity()
                            : "Unknown";

                    response.getWriter().println(
                            "<div class=\"card\" onclick=\"window.location.href='detail?id=" + room.getRoomId() + "'\">"
                            + "<div class=\"card-footer\">"
                            + "<div class=\"image-placeholder\"></div>"
                            + "<h4>" + room.getTitle() + "</h4>"
                            + "<p>" + cityName + "</p>"
                            + "</div></div>"
                    );
                }
                return;
            }
        }

//        // 2. Nearby theo city nếu có
        String city = request.getParameter("city");

        if (city != null && !city.trim().isEmpty()) {
            List<Room> nearbyRooms = new RoomDAO().getRoomsByCity(city);

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            for (Room room : nearbyRooms) {
                String imageUrl = "img/default.jpg";
                if (room.getImages() != null && !room.getImages().isEmpty()) {
                    String[] imgArr = room.getImages().replace("[", "").replace("]", "").replace("\"", "").split(",");
                    imageUrl = imgArr[0].trim();
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "img/" + imageUrl;
                    }
                }

                out.println("<div class='card' onclick=\"window.location.href='detail?id=" + room.getRoomId() + "'\">");
                out.println("  <div class='card-footer'>");
                out.println("    <img class='image-placeholder' src='" + imageUrl + "' alt='Room Image'/>");
                out.println("    <h4>" + room.getTitle() + "</h4>");
                out.println("    <p>" + room.getCity() + "</p>");
                out.println("  </div>");
                out.println("</div>");
            }
            return;
        }

        // 3. Nếu không có type hoặc city, thì load latestRooms
        List<Room> latestRooms = roomDAO.getLatestRooms(5);

        System.out.println("🧪 ==> HomeServlet đã được gọi");
        System.out.println("🧪 Số lượng phòng mới: " + latestRooms.size());

        for (Room r : latestRooms) {
            System.out.println("🔥 Room: " + r.getTitle());
        }

        request.setAttribute("latestRooms", latestRooms);
        request.getRequestDispatcher("home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response); // Xử lý POST như GET
    }

    @Override
    public String getServletInfo() {
        return "Trang chủ hiển thị danh sách phòng mới nhất, theo loại, hoặc theo thành phố (Nearby)";
    }
}
   