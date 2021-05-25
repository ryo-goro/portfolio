package tex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReferencesShower {

    // 定理の証明内での他定理の参照を表示する
    public static void main(String[] args) {

        // 数学ノート（以下、ノート）の相対パス
        String filename = "settheory/settheory2.tex";
        // ノートのPathオブジェクトを生成
        Path path = Paths.get(filename);

        // sthmsInEachSectionListを格納するリスト
        List<List<String>> sthmsList = new ArrayList<>();
        // 各sectionの各定理のラベルを格納するリスト
        List<String> sthmsInEachSectionList = null;
        // referencesInEachSectionListを格納するリスト
        List<List<Set<String>>> referencesList = new ArrayList<>();
        // 各sectionの各定理における参照を格納するリスト
        List<Set<String>> referencesInEachSectionList = null;

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            // 集合論のパートに入ったらtrueになる
            boolean inPart = false;
            // 定理環境内に入ったらtrueになる。その定理の証明に入ったらfalseに戻る
            boolean inSthm = false;
            // 定理環境内に入ったらtrueになる。その定理のラベルを見つけたらfalseに戻る
            boolean labelSearcher = false;
            // 定理の証明に入ったらtrueになる。証明が終了したらfalseに戻る
            boolean inProof = false;
            // 定理の証明内での参照を格納するSet
            Set<String> referencesSet = null;

            // ノート内の特定の文字列に反応するためのパターン
            Pattern sectionPattern = Pattern.compile("\\\\section\\{.*?\\}.*");
            Pattern sthmPattern = Pattern.compile("\\\\begin\\{thm\\}.*");
            Pattern labelPattern = Pattern.compile("\\\\label\\{.*?\\}.*");
            Pattern proofPattern = Pattern.compile("\\\\noindent\\{\\\\bf 証明\\}.*");
            Pattern refPattern = Pattern.compile(".*\\\\ref\\{.*\\}.*");

            // ノートを一行ずつ読み込んでこれに格納する
            String line = null;

            // ノートを一行ずつ、すべて読み込む
            // この処理は一度だけ行われる
            while ((line = reader.readLine()) != null) {

                // 上記の各Patternオブジェクトに対応するMatcherオブジェクトを生成
                Matcher sectionMatcher = sectionPattern.matcher(line);
                Matcher sthmMatcher = sthmPattern.matcher(line);
                Matcher labelMatcher = labelPattern.matcher(line);
                Matcher proofMatcher = proofPattern.matcher(line);
                Matcher refMatcher = refPattern.matcher(line);

                // ノートの集合論部分に入ったらinPartをtrueにする
                // ※ノートの前半部分は集合論では無いので、inPartをfalseにすることで無視している
                if (line.equals("\\part{集合論}")) {
                    inPart = true;
                }

                // ノートの集合論部分に入ってからの処理
                if (inPart) {
                    // sectionを見つけた場合の処理
                    if (sectionMatcher.matches()) {
                        if (sthmsInEachSectionList != null) { // 二週目以降の処理
                            sthmsList.add(sthmsInEachSectionList);
                        }
                        if (referencesInEachSectionList != null) { // 二週目以降の処理
                            referencesList.add(referencesInEachSectionList);
                        }
                        sthmsInEachSectionList = new ArrayList<>();
                        referencesInEachSectionList = new ArrayList<>();
                    }
                    // 定理を見つけた場合の処理
                    if (sthmMatcher.matches()) {
                        inSthm = true; // 定理環境に入ったことを示すboolean
                        labelSearcher = true; // 定理のラベルを拾うためのboolean
                    }

                    // 定理のラベルを見つけた場合の処理
                    if (labelSearcher && labelMatcher.matches()) {
                        // ノートでは定理のラベルは「\\label{ラベル名}」の形で書かれているので、
                        // そこから「ラベル名」を取り出してsthmsInEachSectionListに格納する
                        int begin = line.indexOf("{");
                        int end = line.indexOf("}");
                        sthmsInEachSectionList.add(line.substring(begin + 1, end));
                        labelSearcher = false;
                    }

                    // 定理の証明を見つけた場合の処理
                    if (inSthm && proofMatcher.matches()) {
                        inSthm = false;
                        inProof = true;
                        // 証明内での参照を格納するためのSetオブジェクトを生成
                        referencesSet = new HashSet<>();
                    }

                    // 証明内での処理
                    if (inProof) {
                        // 何かしらの参照がある行を見つけた場合の処理
                        // 参照は必ず「\\ref{参照するラベル名}」の形で行われるので、
                        // そこから「参照するラベル名」を取り出してreferencesSetに格納していく
                        if (refMatcher.matches()) {
                            String[] rawReferences = line.split("\\\\ref\\{");
                            for (int i = 1; i < rawReferences.length; i++) {
                                int end = rawReferences[i].indexOf("}");
                                String reference = rawReferences[i].substring(0, end);
                                referencesSet.add(reference);
                            }
                        }
                        // 証明が終了したらreferencesSetをreferencesInEachSectionListに格納する
                        if (line.equals("\\halmos")) {
                            inProof = false;
                            referencesInEachSectionList.add(referencesSet);
                        }
                    }
                }
            }
            // ノート終端まで読み込み終わった場合の処理
            sthmsList.add(sthmsInEachSectionList);
            referencesList.add(referencesInEachSectionList);
        } catch (NoSuchFileException e) {
            System.out.println("読み込むファイルが見つかりませんでした");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // ここからコンソールに表示するための処理
        System.out.println("---参照確認プログラム---");
        System.out.println();

        // section番号と定理番号をユーザに入力させる
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            int sectionNumber = 0;
            int sthmNumber = 0;
            int numberOfSthms = 0;

            // section番号の入力
            // [q]の入力でプログラムを終了
            while (true) {
                System.out.println("総section数 : " + sthmsList.size());
                System.out.print("section番号を入力してください.");
                System.out.println("終了するには[q]を入力してください.");
                String input = null;
                try {
                    input = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                if ("q".equals(input)) {
                    System.out.println("終了します.");
                    System.exit(0);
                }
                try {
                    sectionNumber = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("入力が正しくありません.");
                    continue;
                }
                if (sectionNumber < 1 || sectionNumber > sthmsList.size()) {
                    System.out.println("指定された番号のsectionは存在しません.");
                    continue;
                }
                numberOfSthms = sthmsList.get(sectionNumber - 1).size();
                if (numberOfSthms == 0) {
                    System.out.println("section" + sectionNumber + "には定理が存在しません.");
                    continue;
                }
                break;
            }

            // 定理番号の入力
            // [q]の入力でプログラムを終了
            while (true) {
                System.out.println("section" + sectionNumber + "内の定理数 : " + numberOfSthms);
                System.out.print("定理番号を入力してください.");
                System.out.println("終了するには[q]を入力してください.");
                String input = null;
                try {
                    input = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                if ("q".equals(input)) {
                    System.out.println("終了します.");
                    System.exit(0);
                }
                try {
                    sthmNumber = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("入力が正しくありません.");
                    continue;
                }
                if (sthmNumber < 1 || sthmNumber > numberOfSthms) {
                    System.out.println("指定された番号の定理は存在しません.");
                    continue;
                }
                break;
            }

            // 指定された定理のラベルをsthmNameに格納
            String sthmName = sthmsList.get(sectionNumber - 1).get(sthmNumber - 1);
            // 指定された定理の証明内での参照をreferencesに格納
            Set<String> references = referencesList.get(sectionNumber - 1).get(sthmNumber - 1);
            // 参照された定理数
            int referenceCounter = 0;
            // 前方参照があるかどうかのboolean
            boolean illegalReference = false;

            System.out.print("定理" + sectionNumber + "." + sthmNumber + "[" + sthmName + "]における他定理の参照");

            // 他定理の参照がある場合の表示
            for (int i = 0; i < sthmsList.size(); i++) {
                for (int j = 0; j < sthmsList.get(i).size(); j++) {
                    String label = sthmsList.get(i).get(j);
                    if (references.contains(label)) {
                        if (sectionNumber < i + 1 || (sectionNumber == i + 1 && sthmNumber < j)) {
                            illegalReference = true;
                        }
                        referenceCounter++;
                        System.out.println();
                        System.out.print(">定理" + (i + 1) + "." + (j + 1) + " : " + label);
                    }
                }
            }

            // 他定理の参照が無い場合の表示
            if (referenceCounter == 0) {
                System.out.println("はありません.");
                continue;
            }
            System.out.println();

            // 他定理の前方参照を行なっていないかどうかを表示
            // 前方参照の例 : 定理2.1の証明で定理2.3を参照している場合など
            // 前方参照がある場合、ノート作成時にミスをしたということなので、ノートの修正が必要
            if (illegalReference) {
                System.err.println("定理の前方参照があります.");
                System.out.println();
            } else {
                System.out.println("定理の前方参照はありません.");
                System.out.println();
            }
        }
    }

}
