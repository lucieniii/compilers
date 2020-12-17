import java.io.*;

public class Main {
    public static int unknow_flag = 1;

    public static void main(String[] args) throws IOException {
        // Scanner scan = new Scanner(System.in);
        String input = "";
        input = args[0];
        // input = scan.next();
        FileInputStream fis = new FileInputStream(input);
        RandomAccessFile raf = new RandomAccessFile(new File(input), "r");
        String s;
        while ((s = raf.readLine()) != null && unknow_flag == 1) {
            System.out.println(s);
        }
        //System.exit(1);
        raf.close();
        fis.close();
    }
}
