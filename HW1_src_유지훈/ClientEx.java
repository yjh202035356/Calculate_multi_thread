package Calculate;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;

public class ClientEx {
    public static void main(String[] args) {
    
        // 필요한 변수 및 리소스 초기화
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        
        try {
            // 서버 정보를 설정하거나 가져오는 기능 추가
            Properties properties = new Properties();
            properties.setProperty("serverIP", "localhost");
            properties.setProperty("serverPort", "8872");

            // 서버 정보를 파일에 저장
            try (OutputStream output = new FileOutputStream("server_info.dat")) {
                properties.store(output, "Server Information");
            }

            // 서버 정보를 파일에서 읽어오기
            try (InputStream input = new FileInputStream("server_info.properties")) {
                properties.load(input);
            }

            // 서버 정보 가져오기
            String serverIP = properties.getProperty("serverIP");
            int serverPort = Integer.parseInt(properties.getProperty("serverPort"));
            
            // 서버에 연결
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            while (true) {
                // 사용자로부터 입력 받은 메시지
                String outputMessage = scanner.nextLine(); 
                
                if (outputMessage.equalsIgnoreCase("bye")) {
                    // 사용자가 "bye"를 입력한 경우 서버로 전송 후 연결 종료
                    out.write(outputMessage + "\n"); 
                    out.flush();
                    break;
                }
                
                // 사용자 입력을 서버로 전송
                out.write(outputMessage + "\n");
                out.flush();
                
                // 서버로부터 계산 결과 수신
                String inputMessage = in.readLine(); 
                System.out.println(inputMessage);
            }
        } catch (IOException e) {
            // 예외 처리: 입출력 오류
            System.out.println(e.getMessage());
        } finally {
            try {
                // 리소스 정리
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error while closing resources: " + e.getMessage());
            }
        }
    }
}
