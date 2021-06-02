package tex;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IllegalReferencesChecker {

    public static void main(String[] args) {

        String filename = "settheory/settheory2.tex";
        Path filepath = Paths.get(filename);

        List<List<String>> sthms = new ArrayList<>();
        List<String> sthmsInEachSection = null;
        List<List<Set<String>>> references = new ArrayList<>();
        List<Set<String>> referencesInEachSection = null;
        Set<String> referencesInEachProof = null;

        Pattern sectionPattern = Pattern.compile("\\\\section\\{.*\\}.*");
        Pattern sthmPattern = Pattern.compile("\\\\begin\\{thm\\}.*");
        Pattern labelPattern = Pattern.compile(".*\\\\label\\{.*\\}.*");
        Pattern refPattern = Pattern.compile(".*\\\\ref\\{.*\\}.*");

        try (BufferedReader reader = Files.newBufferedReader(filepath)) {

            boolean inPart = false;
            boolean inSthm = false;
            boolean labelGetter = false;
            boolean inProof = false;

            String line = null;

            while ((line = reader.readLine()) != null) {
                Matcher sectionMatcher = sectionPattern.matcher(line);
                Matcher sthmMatcher = sthmPattern.matcher(line);
                Matcher labelMatcher = labelPattern.matcher(line);
                Matcher refMatcher = refPattern.matcher(line);

                if (!inPart && line.matches("\\\\part\\{集合論\\}.*")) {
                    inPart = true;
                }

                if (inPart) {
                    if (sectionMatcher.matches()) {
                        if (sthmsInEachSection != null) {
                            sthms.add(sthmsInEachSection);
                        }
                        if (referencesInEachSection != null) {
                            references.add(referencesInEachSection);
                        }
                        sthmsInEachSection = new ArrayList<>();
                        referencesInEachSection = new ArrayList<>();
                    }

                    if (line.equals("\\end{document}")) {
                        sthms.add(sthmsInEachSection);
                        references.add(referencesInEachSection);
                    }

                    if (sthmMatcher.matches()) {
                        inSthm = true;
                        labelGetter = true;
                        referencesInEachProof = new HashSet<>();
                    }

                    if (labelGetter && labelMatcher.matches()) {
                        int begin = line.indexOf("{");
                        int end = line.indexOf("}");
                        String label = line.substring(begin + 1, end);
                        sthmsInEachSection.add(label);
                        labelGetter = false;
                    }

                    if (inSthm && line.matches("\\\\noindent\\{\\\\bf 証明\\}.*")) {
                        inSthm = false;
                        inProof = true;
                    }

                    if (inProof) {
                        if (refMatcher.matches()) {
                            String[] rawReferences = line.split("\\\\ref\\{");
                            for (int i = 1; i < rawReferences.length; i++) {
                                int end = rawReferences[i].indexOf("}");
                                String reference = rawReferences[i].substring(0, end);
                                referencesInEachProof.add(reference);
                            }
                        }
                        if (line.matches("\\\\halmos.*")) {
                            referencesInEachSection.add(referencesInEachProof);
                            inProof = false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<String> illegalSthms = new ArrayList<>();

        for (int i = 0; i < references.size(); i++) {
            for (int j = 0; j < references.get(i).size(); j++) {
                for (int k = 0; k < sthms.size(); k++) {
                    for (int l = 0; l < sthms.get(k).size(); l++) {
                        if (references.get(i).get(j).contains(sthms.get(k).get(l))) {
                            if (i < k || (i == k && j < l)) {
                                illegalSthms.add((i + 1) + "." + (j + 1) + " " + sthms.get(i).get(j));
                            }
                        }
                    }
                }
            }
        }

        if (illegalSthms.size() > 0) {
            System.out.println("以下の定理の証明において不正な参照があります:");
            for (String illegalSthm : illegalSthms) {
                System.out.println("定理" + illegalSthm);
            }
        } else {
            System.out.println("不正な参照はありません.");
        }

    }

}
