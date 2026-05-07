package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Integer> getAllCoursesIds() {
        List<Integer> ids = new ArrayList<Integer>();
        String sql = "SELECT course_id FROM courses";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (Statement stat = conn.createStatement();
                 ResultSet rs = stat.executeQuery(sql)) {

                while (rs.next()) {
                    int courseId = rs.getInt("course_id");
                    ids.add(courseId);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return ids;
    }
}