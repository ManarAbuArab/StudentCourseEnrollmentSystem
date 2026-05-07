package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import models.Enrollment;

public class EnrollmentDAO {

    public List<Enrollment> findAll() {
        List<Enrollment> list = new ArrayList<Enrollment>();
        String sql = "SELECT * FROM enrollment";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (Statement stat = conn.createStatement();
                 ResultSet rs = stat.executeQuery(sql)) {

                while (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    int studentId = rs.getInt("student_id");
                    int courseId = rs.getInt("course_id");
                    String enrollmentDate = rs.getString("enrollment_date");

                    Enrollment e = new Enrollment(
                            enrollmentId,
                            studentId,
                            courseId,
                            enrollmentDate
                    );

                    list.add(e);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public boolean isDuplicateEnrollment(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE student_id = ? AND course_id = ?";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, courseId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean isDuplicateEnrollmentForUpdate(int enrollmentId, int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollment "
                + "WHERE student_id = ? AND course_id = ? AND enrollment_id <> ?";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, courseId);
                ps.setInt(3, enrollmentId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean insertOne(Enrollment e) {
        if (isDuplicateEnrollment(e.getStudentId(), e.getCourseId())) {
            return false;
        }

        String sql = "INSERT INTO enrollment(student_id, course_id, enrollment_date) "
                + "VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, e.getStudentId());
                ps.setInt(2, e.getCourseId());
                ps.setString(3, e.getEnrollmentDate());

                int rows = ps.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean updateOne(Enrollment e) {
        if (isDuplicateEnrollmentForUpdate(
                e.getEnrollmentId(),
                e.getStudentId(),
                e.getCourseId())) {
            return false;
        }

        String sql = "UPDATE enrollment SET student_id = ?, course_id = ?, enrollment_date = ? "
                + "WHERE enrollment_id = ?";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, e.getStudentId());
                ps.setInt(2, e.getCourseId());
                ps.setString(3, e.getEnrollmentDate());
                ps.setInt(4, e.getEnrollmentId());

                int rows = ps.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean deleteOne(Enrollment e) {
        String sql = "DELETE FROM enrollment WHERE enrollment_id = ?";

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, e.getEnrollmentId());

                int rows = ps.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}