package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class QuestionUploader {

    public static int uploadQuestion(Connection connection, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        String query = "INSERT INTO questions (content, difficulty, topic_id, question_type_id, score, " +
                "image_path, audio_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, content);
            statement.setString(2, difficulty);
            statement.setInt(3, GetId.getTopicId(topic));
            statement.setInt(4, GetId.getTypeID(type));
            statement.setInt(5, score);
            statement.setString(6, imagePath);
            statement.setString(7, audioPath);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                // 获取生成的题目ID
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            throw new SQLException("不能添加问题！");
        }
    }
    public static void uploadOptions(Connection connection, int questionId, String optionA, String optionB, String optionC, String optionD, boolean isCorrectA, boolean isCorrectB, boolean isCorrectC, boolean isCorrectD) throws SQLException {
        // 依次保存 A、B、C、D 选项到数据库
        saveOption(connection, questionId, optionA, isCorrectA, "A");
        saveOption(connection, questionId, optionB, isCorrectB, "B");
        saveOption(connection, questionId, optionC, isCorrectC, "C");
        saveOption(connection, questionId, optionD, isCorrectD, "D");
    }

    private static void saveOption(Connection connection, int questionId, String optionContent, boolean isCorrect, String optionName) throws SQLException {
        String query = "INSERT INTO options (question_id, option_text, is_correct, option_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, questionId);
            statement.setString(2, optionContent);
            statement.setBoolean(3, isCorrect);
            statement.setString(4, optionName);

            statement.executeUpdate();
        }
    }
    public static void updateOptions(Connection connection, int questionId, String optionA, String optionB, String optionC, String optionD, boolean isCorrectA, boolean isCorrectB, boolean isCorrectC, boolean isCorrectD) throws SQLException {
        // 更新 A、B、C、D 选项
        updateOption(connection, questionId, optionA, isCorrectA, "A");
        updateOption(connection, questionId, optionB, isCorrectB, "B");
        updateOption(connection, questionId, optionC, isCorrectC, "C");
        updateOption(connection, questionId, optionD, isCorrectD, "D");
    }

    private static void updateOption(Connection connection, int questionId, String optionContent, boolean isCorrect, String optionName) throws SQLException {
        String query = "UPDATE options SET option_text=?, is_correct=? WHERE question_id=? AND option_name=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, optionContent);
            statement.setBoolean(2, isCorrect);
            statement.setInt(3, questionId);
            statement.setString(4, optionName);

            statement.executeUpdate();
        }
    }


    public static void updateAnswer(Connection connection, int questionId, String answerText) throws SQLException {
        String query = "UPDATE answers SET correct_answer_text=? WHERE question_id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, answerText);
            statement.setInt(2, questionId);

            statement.executeUpdate();
        }
    }
    public static void saveAnswer(Connection connection, int questionId, String answerText) throws SQLException {
        String query = "INSERT INTO answers (question_id, correct_answer_text) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, questionId);
            statement.setString(2, answerText);

            statement.executeUpdate();
        }
    }

    // 修改 QuestionUploader 类的 uploadOptions 方法
    public static void uploadMultiOptions(Connection connection, int questionId, String optionA, String optionB, String optionC, String optionD, Set<String> selectedOptions) throws SQLException {
        String query = "INSERT INTO options (question_id, option_text, is_correct, option_name) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // 添加选项 A
            statement.setInt(1, questionId);
            statement.setString(2, optionA);
            statement.setBoolean(3, selectedOptions.contains("A"));
            statement.setString(4, "A");
            statement.executeUpdate();

            // 添加选项 B
            statement.setInt(1, questionId);
            statement.setString(2, optionB);
            statement.setBoolean(3, selectedOptions.contains("B"));
            statement.setString(4, "B");
            statement.executeUpdate();

            // 添加选项 C
            statement.setInt(1, questionId);
            statement.setString(2, optionC);
            statement.setBoolean(3, selectedOptions.contains("C"));
            statement.setString(4, "C");
            statement.executeUpdate();

            // 添加选项 D
            statement.setInt(1, questionId);
            statement.setString(2, optionD);
            statement.setBoolean(3, selectedOptions.contains("D"));
            statement.setString(4, "D");
            statement.executeUpdate();
        }
    }

    public static void updateMultiOptions(Connection connection, int questionId, String optionA, String optionB, String optionC, String optionD, Set<String> selectedOptions) throws SQLException {
        String query = "UPDATE options SET option_text = ?, is_correct = ? WHERE question_id = ? AND option_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // 添加选项 A
            statement.setString(1, optionA);
            statement.setBoolean(2, selectedOptions.contains("A"));
            statement.setInt(3, questionId);
            statement.setString(4, "A");
            statement.executeUpdate();

            // 添加选项 B
            statement.setString(1, optionB);
            statement.setBoolean(2, selectedOptions.contains("B"));
            statement.setInt(3, questionId);
            statement.setString(4, "B");
            statement.executeUpdate();

            // 添加选项 C
            statement.setString(1, optionC);
            statement.setBoolean(2, selectedOptions.contains("C"));
            statement.setInt(3, questionId);
            statement.setString(4, "C");
            statement.executeUpdate();

            // 添加选项 D
            statement.setString(1, optionD);
            statement.setBoolean(2, selectedOptions.contains("D"));
            statement.setInt(3, questionId);
            statement.setString(4, "D");
            statement.executeUpdate();
        }
    }

    public static void uploadTopic(Connection connection, String subject, String topic)throws SQLException {
        int subjectId = GetId.getSubjectId(subject);
        String query = "INSERT INTO topics ( subject_id, name ) VALUES ( ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, subjectId);
            statement.setString(2, topic);
            statement.executeUpdate();
        }
    }

    public static void uploadSubject(Connection connection, String subject)throws SQLException {
        String query = "INSERT INTO subjects ( subject ) VALUES ( ? )";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject);
            statement.executeUpdate();
        }
    }
}
