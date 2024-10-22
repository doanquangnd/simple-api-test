import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestSender extends JFrame {

    private JTextField inPathField;
    private JTextField outPathField;
    private JTextField urlField;
    private JTextField contentTypeField;
    private JButton sendButton;
    private JLabel statusLabel;

    public HttpRequestSender() {
        setTitle("HTTP Request Sender");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel inPathLabel = new JLabel("Input File Path:");
        inPathLabel.setBounds(20, 20, 100, 25);
        add(inPathLabel);

        inPathField = new JTextField();
        inPathField.setBounds(150, 20, 300, 25);
        add(inPathField);

        JLabel outPathLabel = new JLabel("Output File Path:");
        outPathLabel.setBounds(20, 60, 100, 25);
        add(outPathLabel);

        outPathField = new JTextField();
        outPathField.setBounds(150, 60, 300, 25);
        add(outPathField);

        JLabel urlLabel = new JLabel("URL:");
        urlLabel.setBounds(20, 100, 100, 25);
        add(urlLabel);

        urlField = new JTextField();
        urlField.setBounds(150, 100, 300, 25);
        add(urlField);

        JLabel contentTypeLabel = new JLabel("Content-Type:");
        contentTypeLabel.setBounds(20, 140, 100, 25);
        add(contentTypeLabel);

        contentTypeField = new JTextField();
        contentTypeField.setBounds(150, 140, 300, 25);
        add(contentTypeField);

        sendButton = new JButton("Send Request");
        sendButton.setBounds(150, 180, 150, 30);
        add(sendButton);

        statusLabel = new JLabel("");
        statusLabel.setBounds(20, 220, 500, 25);
        add(statusLabel);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendHttpRequest();
            }
        });
    }

    private void sendHttpRequest() {
        String inPath = inPathField.getText();
        String outPath = outPathField.getText();
        String urlString = urlField.getText();
        String contentType = contentTypeField.getText();

        try {
            // Đọc nội dung từ file input
            String bodyData = readFileContent(inPath);

            // Tạo URL và mở kết nối HTTP
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); // Mặc định là POST
            conn.setRequestProperty("Content-Type", contentType);
            conn.setDoOutput(true);

            // Gửi dữ liệu
            OutputStream os = conn.getOutputStream();
            os.write(bodyData.getBytes("Shift_JIS"));
            os.flush();
            os.close();

            // Đọc phản hồi từ server
            int responseCode = conn.getResponseCode();
            String responseMessage = readResponse(conn);

            // Lưu phản hồi ra file output
            writeFileContent(outPath, responseMessage);

            // Hiển thị trạng thái thành công
            statusLabel.setText("Request completed! Response Code: " + responseCode);

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    // Đọc nội dung từ file
    private String readFileContent(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    // Lưu nội dung vào file
    private void writeFileContent(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(content);
        writer.close();
    }

    // Đọc phản hồi từ server
    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "Shift_JIS"));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HttpRequestSender().setVisible(true);
            }
        });
    }
}
