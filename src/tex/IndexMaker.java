package tex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexMaker {

    // コマンドラインの第一引数で指定したsectionの定理一覧を作成する
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("コマンドライン引数にsectionNumberを指定してください");
            return;
        }

        // コマンドラインの第一引数に指定したsectionNumberをここに入れる
        // 第二引数以降は無視する
        int sectionNumber = 0;

        try {
            sectionNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("コマンドライン引数にsectionNumberを指定してください");
            return;
        }
        if (sectionNumber < 1) {
            System.out.println("指定されたsectionNumberに対応するsectionが存在しません");
            return;
        }

        // 読み込むファイルのパス
        String filename = "settheory/settheory2.tex";
        // 定理一覧ファイルを入れるディレクトリのパス
        String directoryname = "index/section" + sectionNumber;
        // preambleファイルのパス
        String preamble = "text/preamble.txt";

        int sectionCounter = 0; // 集合論のパート内でのsectionのカウンタ
        int sthmCounter = 0; // 指定したsection内での定理のカウンタ
        int equationCounter = 0; // 指定したsection内での番号付き式のカウンタ
        boolean inPart = false; // 集合論のパートに入っているかどうか
        boolean inSthm = false; // 指定したsection内で、定理環境に入っているかどうか
        boolean inEquation = false; // 指定したsection内で、番号付き式を出力する式環境に入っているかどうか
        boolean temp = false; // 定理の前にlabelを表示するためのboolean
        String sectionname = null; // 指定したsectionの名前
        String vspace = "\\vspace{-3mm}"; // 定理のlabelと定理の間の空白を調整（デフォルトだと開きすぎなので近くする）

        // 正規表現を用いて特定の文字列に反応する
        Pattern sectionPattern = Pattern.compile("\\\\section\\{.*?\\}.*");
        Pattern beginSthmPattern = Pattern.compile("\\\\begin\\{thm\\}.*");
        Pattern endSthmPattern = Pattern.compile("\\\\end\\{thm\\}.*");
        Pattern beginEquationPattern = Pattern.compile("\\\\begin\\{(equation|align|multline)\\}.*");
        Pattern endEquationPattern = Pattern.compile("\\\\end\\{(equation|align|multline)\\}.*");
        Pattern labelPattern = Pattern.compile(".*?\\\\label\\{.*?\\}.*");

        Path readerPath = Paths.get(filename);
        Path preaderPath = Paths.get(preamble);
        Path dirPath = Paths.get(directoryname);
        Path writerPath = Paths.get(directoryname + "/section" + sectionNumber + "index.tex");

        BufferedReader reader = null;
        BufferedReader preader = null;
        BufferedWriter writer = null;

        try {
            reader = Files.newBufferedReader(readerPath);

            String line = null;

            // settheory2.texを終端に達するまで一行ずつ読み込み、lineに格納
            while ((line = reader.readLine()) != null) {
                Matcher sectionMatcher = sectionPattern.matcher(line);
                Matcher beginSthmMatcher = beginSthmPattern.matcher(line);
                Matcher endSthmMatcher = endSthmPattern.matcher(line);
                Matcher beginEquationMatcher = beginEquationPattern.matcher(line);
                Matcher endEquationMatcher = endEquationPattern.matcher(line);
                Matcher labelMatcher = labelPattern.matcher(line);

                if (line.equals("\\part{集合論}")) {
                    inPart = true;
                }
                if (inPart) {
                    if (sectionMatcher.matches()) {
                        sectionCounter++;
                        if (sectionNumber == sectionCounter) {
                            sectionname = line;
                        }
                    }
                    if (sectionNumber == sectionCounter) {
                        if (beginSthmMatcher.matches()) {
                            // 指定したsection内で初めて定理が見つかったときの処理:
                            // 1) 定理一覧ファイルを納めるディレクトリを作成（既にディレクトリが存在する場合、中身を全削除）
                            // 2) 1)のディレクトリ内に定理一覧ファイルを作成
                            // 3) 2)のファイルにプリアンブルを書く
                            // 4) section番号を指定されたものにする
                            if (sthmCounter == 0) {
                                // 1)の処理
                                if (Files.exists(dirPath)) {
                                    File dir = dirPath.toFile();
                                    File[] files = dir.listFiles();
                                    for (File file : files) {
                                        file.delete();
                                    }
                                    System.out.println("ディレクトリ : " + directoryname + "を上書きしました");
                                } else {
                                    Files.createDirectory(dirPath);
                                    System.out.println("ディレクトリ : " + directoryname + "を作成しました");
                                }

                                // 2)の処理
                                writer = Files.newBufferedWriter(writerPath);
                                System.out.println(
                                        "ファイル : " + directoryname + "/section" + sectionNumber + "index.texを作成しました");

                                // 3), 4)の処理
                                preader = Files.newBufferedReader(preaderPath);
                                String pline = null;
                                while ((pline = preader.readLine()) != null) {
                                    writer.append(pline);
                                    writer.newLine();
                                }
                                writer.newLine();
                                writer.newLine();
                                writer.newLine();
                                writer.newLine();
                                writer.append("\\setcounter{section}{" + (sectionNumber - 1) + "}");
                                writer.newLine();
                                writer.append(sectionname);
                                writer.newLine();
                                writer.newLine();
                                writer.newLine();
                                writer.newLine();
                                writer.newLine();
                            }

                            writer.append("\\setcounter{equation}{" + equationCounter + "}");
                            writer.newLine();
                            writer.append("\\mbox{}");
                            writer.newLine();
                            writer.newLine();
                            writer.newLine();
                            writer.newLine();
                            writer.newLine();

                            sthmCounter++;
                            temp = true;
                        }

                        if (inSthm) {
                            writer.append(line);
                            writer.newLine();
                        }

                        if (temp && labelMatcher.matches()) {
                            inSthm = temp;
                            temp = false;
                            int begin = line.indexOf("{");
                            int end = line.indexOf("}");
                            String label = line.substring(begin + 1, end).replace("&", "\\&");
                            writer.append("\\noindent[" + label + "]" + vspace);
                            writer.newLine();
                            writer.append("\\begin{thm}");
                            writer.newLine();
                            writer.append(line.substring(0, end + 1) + "%定理" + sectionNumber + "." + sthmCounter);
                            writer.newLine();
                        }

                        if (endSthmMatcher.matches()) {
                            inSthm = false;
                            writer.newLine();
                            writer.newLine();
                            writer.newLine();
                            writer.newLine();
                        }
                        if (beginEquationMatcher.matches()) {
                            inEquation = true;
                        }
                        if (endEquationMatcher.matches()) {
                            inEquation = false;
                        }
                        if (inEquation && labelMatcher.matches()) {
                            equationCounter++;
                        }
                    }

                    if (sectionNumber < sectionCounter) {
                        break;
                    }
                }
            }
            if (writer == null) {
                System.out.println("section" + sectionNumber + "は存在しないか、またはその中に定理がありません");
            } else {
                writer.append("\\end{document}");
                System.out.println("定理数 : " + sthmCounter);
                System.out.println("式数 : " + equationCounter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (preader != null) {
                try {
                    preader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}