package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetId {

    public static int getSubjectId(String subject) {
        return getId("SELECT id FROM subjects WHERE subject = ?", subject);
    }
    public static int getUserId(String userName) {
        return getId("SELECT id FROM login WHERE name = ?", userName);
    }
    public static int getTopicId(String topic){
        return getId("SELECT id FROM topics WHERE name = ?", topic);
    }
    public static int getTypeID(String type){
        return getId("SELECT id FROM question_types WHERE name = ?", type);
    }
    public static int getQuestionID(String content){
        return getId("SELECT id FROM questions WHERE content = ?", content);
    }
    public static int getTypeID1(String content){
        String query = "SELECT question_type_id FROM questions WHERE content = ?";
        try (Connection connection = DatabaseConnector.connect()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, content);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("question_type_id");
                    }
                    // 处理没有结果的情况
                    return -1;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    private static int getId(String query, String parameter) {
        try (Connection connection = DatabaseConnector.connect()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, parameter);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                    // 处理没有结果的情况
                    return -1;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }
}
