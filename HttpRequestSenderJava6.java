import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpRequestSender extends JFrame {

    private JTextField inPathField;
    private JTextField outPathField;
    private JTextField urlField;
    private JTextField contentTypeField;
    private JButton sendButton;
    private JTextArea statusTextArea;

    public HttpRequestSender() {
        setTitle("HTTP Request Sender");
        setSize(820, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Content-Type label and field
        JLabel contentTypeLabel = new JLabel("Content-Type:");
        contentTypeLabel.setBounds(20, 20, 100, 25);
        add(contentTypeLabel);

        contentTypeField = new JTextField("application/x-www-form-urlencoded");
        contentTypeField.setBounds(150, 20, 300, 25);
        add(contentTypeField);

        // Input File Path label and field
        JLabel inPathLabel = new JLabel("Input File Path:");
        inPathLabel.setBounds(20, 60, 100, 25);
        add(inPathLabel);

        inPathField = new JTextField();
        inPathField.setBounds(150, 60, 300, 25);
        add(inPathField);

        JButton selectInputButton = new JButton("Select Input File");
        selectInputButton.setBounds(460, 60, 120, 25);
        add(selectInputButton);

        selectInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    inPathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        // Output File Path label and field
        JLabel outPathLabel = new JLabel("Output File Path:");
        outPathLabel.setBounds(20, 100, 100, 25);
        add(outPathLabel);

        outPathField = new JTextField();
        outPathField.setBounds(150, 100, 300, 25);
        add(outPathField);

        JButton selectOutputButton = new JButton("Select Output Path");
        selectOutputButton.setBounds(460, 100, 120, 25);
        add(selectOutputButton);

        selectOutputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser directoryChooser = new JFileChooser();
                directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = directoryChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = directoryChooser.getSelectedFile();
                    outPathField.setText(selectedDirectory.getAbsolutePath());
                }
            }
        });

        // URL label and field
        JLabel urlLabel = new JLabel("URL:");
        urlLabel.setBounds(20, 140, 100, 25);
        add(urlLabel);

        urlField = new JTextField();
        urlField.setBounds(150, 140, 300, 25);
        add(urlField);

        // Status label (multi-line) to display results
        statusTextArea = new JTextArea();
        statusTextArea.setBounds(20, 180, 760, 60);
        statusTextArea.setLineWrap(true);  // Tự động xuống dòng
        statusTextArea.setWrapStyleWord(true);  // Xuống dòng theo từ
        statusTextArea.setEditable(false);
        add(statusTextArea);

        // Send Request button
        sendButton = new JButton("Send Request");
        sendButton.setBounds(150, 260, 150, 30);
        add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    sendHttpRequest();
                }
            }
        });

        // Version and Author label
        JLabel versionLabel = new JLabel("Version 1.0 - Author: John Doe");
        versionLabel.setBounds(20, 420, 300, 25);
        add(versionLabel);
    }

    // Method to validate input
    private boolean validateInput() {
        String inPath = inPathField.getText();
        String outPath = outPathField.getText();
        String urlString = urlField.getText();
        String contentType = contentTypeField.getText();

        if (inPath == null || inPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input File Path cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (outPath == null || outPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Output File Path cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (urlString == null || urlString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "URL cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (contentType == null || contentType.isEmpty()) {
            contentTypeField.setText("application/x-www-form-urlencoded");
        }

        return true;
    }

    // Method to send the HTTP request
    private void sendHttpRequest() {
        String inPath = inPathField.getText();
        String outPath = outPathField.getText();
        String urlString = urlField.getText();
        String contentType = contentTypeField.getText();

        try {
            // Read content from input file
            String bodyData = readFileContent(inPath);

            // Open HTTP connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", contentType);
            conn.setDoOutput(true);

            // Send data
            OutputStream os = conn.getOutputStream();
            os.write(bodyData.getBytes("UTF-8"));
            os.flush();
            os.close();

            // Read response
            int responseCode = conn.getResponseCode();
            String responseMessage = readResponse(conn);

            // Write response to output file
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String outputFileName = "ResponseFile" + timestamp + ".txt";
            File outputFile = new File(outPath, outputFileName);
            writeFileContent(outputFile.getAbsolutePath(), responseMessage);

            // Display success message
            statusTextArea.setText("Request completed! Response Code: " + responseCode);

        } catch (Exception ex) {
            // Log error to file
            String errorFileName = "ErrorLog.txt";
            File errorFile = new File(outPath, errorFileName);
            PrintWriter pw = null;
            BufferedWriter bw = null;
            FileWriter fw = null;

            try {
                fw = new FileWriter(errorFile, true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);

                ex.printStackTrace(pw);

            } catch (IOException ioEx) {
                statusTextArea.setText("Failed to write error log: " + ioEx.getMessage());
            } finally {
                if (pw != null) pw.close();
            }

            // Display error message
            statusTextArea.setText("Error: " + ex.getMessage());
        }
    }

    // Read file content
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

    // Write content to file
    private void writeFileContent(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(content);
        writer.close();
    }

    // Read response from server
    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
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
