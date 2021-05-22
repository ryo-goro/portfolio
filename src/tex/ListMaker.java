package tex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListMaker {

    // 全定理のリストを得る
    public static void main(String[] args) {

        String filename = "settheory/settheory2.tex";
        String listname = "text/sthmlist.txt";
        Path readerPath = Paths.get(filename);
        Path writerPath = Paths.get(listname);

        String message = "作成";
        if (Files.exists(writerPath)) {
            message = "上書き";
        }

        try (BufferedReader reader = Files.newBufferedReader(readerPath);
                BufferedWriter writer = Files.newBufferedWriter(writerPath)) {
            int sectionCounter = 0;
            int sthmCounter = 0;
            boolean inPart = false;
            boolean inSthm = false;

            Pattern sectionPattern = Pattern.compile("\\\\section\\{.*?\\}.*");
            Pattern sthmPattern = Pattern.compile("\\\\begin\\{thm\\}.*");
            Pattern labelPattern = Pattern.compile("\\\\label\\{.*?\\}.*");

            String line = null;

            while ((line = reader.readLine()) != null) {
                Matcher sectionMatcher = sectionPattern.matcher(line);
                Matcher sthmMatcher = sthmPattern.matcher(line);
                Matcher labelMatcher = labelPattern.matcher(line);

                if (line.equals("\\part{集合論}")) {
                    inPart = true;
                }

                if (inPart) {
                    if (sectionMatcher.matches()) {
                        sectionCounter++;
                        sthmCounter = 0;
                        int begin = line.indexOf("{");
                        int end = line.indexOf("}");
                        String sectionname = line.substring(begin + 1, end);
                        if (sectionCounter != 1) {
                            writer.newLine();
                            writer.newLine();
                        }
                        writer.append("section" + sectionCounter + " " + sectionname);
                    }

                    if (sthmMatcher.matches()) {
                        sthmCounter++;
                        inSthm = true;
                    }

                    if (inSthm && labelMatcher.matches()) {
                        inSthm = false;
                        int begin = line.indexOf("{");
                        int end = line.indexOf("}");
                        String label = line.substring(begin + 1, end);
                        writer.newLine();
                        writer.append("定理" + sectionCounter + "." + sthmCounter + " " + label);
                    }
                }
            }
            System.out.println(listname + "を" + message + "しました");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
