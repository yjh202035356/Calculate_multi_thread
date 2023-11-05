package Calculate;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerEx {
    private String serverIP;
    private int serverPort;

    public ServerEx() {
        // 기본 서버 정보 설정
        serverIP = "localhost";
        serverPort = 8872;
    }

    public String getServerInfo() {
        // 현재 서버의 IP 주소와 포트 번호를 반환하는 메서드입니다.
        return "Server IP: " + serverIP + ", Port: " + serverPort;
    }

    public void setServerInfo(String ip, int port) {
        // 서버의 IP 주소와 포트 번호를 설정하는 메서드입니다.
        serverIP = ip;
        serverPort = port;
    }

    public enum ErrorType {
        TOO_MANY_ARGUMENTS,
        INVALID_OPERANDS,
        DIVIDED_BY_ZERO,
        UNKNOWN_OPERATION
    }

    public static String calc(String exp) {
        // 수식 문자열을 토큰으로 분리합니다.
        StringTokenizer st = new StringTokenizer(exp, " ");
        
        // 토큰 수가 3개가 아니면 오류 유형 "TOO_MANY_ARGUMENTS" 처리
        if (st.countTokens() != 3)
            return handleError(ErrorType.TOO_MANY_ARGUMENTS);

        String res = "";

        String opcode = st.nextToken();
        String op1Token = st.nextToken();
        String op2Token = st.nextToken();

        try {
            int op1 = Integer.parseInt(op1Token);
            int op2 = Integer.parseInt(op2Token);

            // 연산 코드(opcode)에 따라 연산을 수행합니다.
            switch (opcode) {
                case "ADD":
                    res = "Answer: " + Integer.toString(op1 + op2);
                    break;
                case "MIN":
                    res = "Answer: " + Integer.toString(op1 - op2);
                    break;
                case "MUL":
                    res = "Answer: " + Integer.toString(op1 * op2);
                    break;
                case "DIV":
                    // 0으로 나누는 경우 "DIVIDED_BY_ZERO" 오류 처리
                    if (op2 == 0) {
                        return handleError(ErrorType.DIVIDED_BY_ZERO);
                    } else {
                        res = "Answer: " + Integer.toString(op1 / op2);
                    }
                    break;
                default:
                    // 알 수 없는 연산 코드인 경우 "UNKNOWN_OPERATION" 오류 처리
                    return handleError(ErrorType.UNKNOWN_OPERATION);
            }
        } catch (NumberFormatException e) {
            // 부적절한 피연산자가 있는 경우 "INVALID_OPERANDS" 오류 처리
            return handleError(ErrorType.INVALID_OPERANDS);
        }

        // 계산 결과 문자열을 반환합니다.
        return res;
    }

    private static String handleError(ErrorType errorType) {
        // 에러 유형에 따라 의미 있는 코드 및 처리를 반환
        switch (errorType) {
            case TOO_MANY_ARGUMENTS:
                return "Error message: too many arguments";
            case INVALID_OPERANDS:
                return "Error message: invalid operands";
            case DIVIDED_BY_ZERO:
                return "Error message: divided by zero";
            case UNKNOWN_OPERATION:
                return "Unknown operation";
            default:
                return "Unknown error";
        }
    }

    public static void main(String[] args) throws Exception {
        ServerEx server = new ServerEx();
        System.out.println(server.getServerInfo());

        // 서버 소켓을 생성하고 클라이언트 연결을 대기합니다.
        ServerSocket listener = new ServerSocket(8872);
        System.out.println("The capitalization server is running...");
        // 스레드 풀을 생성하여 다중 클라이언트 요청을 처리합니다.
        ExecutorService pool = Executors.newFixedThreadPool(20);
        while (true) {
            Socket sock = listener.accept();
            pool.execute(new ClientHandler(sock));
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // 클라이언트와 통신을 위한 입출력 스트림을 설정합니다.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String inputMessage;

                while (true) {
                    // 클라이언트로부터 메시지를 읽어옵니다.
                    inputMessage = in.readLine();
                    System.out.println("Received: " + inputMessage);
                    // 클라이언트가 "bye"를 보내면 연결을 종료하고 대기 상태로 돌아갑니다.
                    if (inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println("Client disconnected: " + socket.getInetAddress());
                        break;
                    }
                    System.out.println(inputMessage); // 받은 메시지를 화면에 출력
                    String response = calc(inputMessage); // 클라이언트 요청을 처리하고 결과를 반환합니다.
                    out.write(response + "\n"); // 결과를 클라이언트에게 보냅니다.
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            }
        }
    }
}
