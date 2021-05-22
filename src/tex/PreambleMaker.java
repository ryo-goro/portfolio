package tex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PreambleMaker {

    // textsフォルダ内にpreamble.txtテキストを作成する
    public static void main(String[] args) {
        String filename = "settheory/settheory2.tex";
        String preamble = "text/preamble.txt";
        Path readerPath = Paths.get(filename);
        Path writerPath = Paths.get(preamble);

        String message = "作成";
        if (Files.exists(writerPath)) {
            message = "上書き";
        }

        try (BufferedReader reader = Files.newBufferedReader(readerPath);
                BufferedWriter writer = Files.newBufferedWriter(writerPath)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                if (line.matches("\\\\begin\\{document\\}.*")) {
                    break;
                }
                writer.newLine();
            }
            System.out.println(preamble + "を" + message + "しました");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
