import java.nio.file.*;
import java.io.*;
import java.util.regex.*;

public class ParseTiles {
    public static void main(String[] args) throws Exception {
        String data = new String(Files.readAllBytes(Paths.get("c:\\Users\\agr_b\\MapRandomizer\\rust\\maprando\\src\\patch\\map_tiles.rs")));
        StringBuilder javaCode = new StringBuilder("package com.maprando.patch;\n\npublic class MapTilesArrays {\n");

        Pattern arrRegex = Pattern.compile("let (\\w+): \\[\\[u8; 8\\]; 8\\] = \\[(.*?)\\];", Pattern.DOTALL);
        Matcher m1 = arrRegex.matcher(data);
        while (m1.find()) {
            String name = m1.group(1);
            String content = m1.group(2).replace("[", "{").replace("]", "}");
            content = content.replaceAll("\\bb\\b", "15").replaceAll("\\bw\\b", "12");
            javaCode.append("    public static final int[][] ").append(name.toUpperCase()).append(" = {\n")
                    .append("        ").append(content.trim()).append("\n    };\n\n");
        }

        Pattern heatRegex = Pattern.compile("MapTileSpecialType::(\\w+) => \\{\\s*data = apply_heat\\(\\[\\s*(.*?)\\s*\\]\\);\\s*\\}", Pattern.DOTALL);
        Matcher m2 = heatRegex.matcher(data);
        while (m2.find()) {
            String name = m2.group(1).replaceAll("(.)([A-Z])", "$1_$2").toUpperCase();
            String content = m2.group(2).replace("[", "{").replace("]", "}");
            javaCode.append("    public static final int[][] ").append(name).append(" = {\n")
                    .append("        ").append(content.trim()).append("\n    };\n\n");
        }

        Pattern updRegex2 = Pattern.compile("update_tile\\(\\s*&mut data,\\s*\\d+,\\s*&vec!\\[(.*?)\\]\\s*,?\\s*\\);", Pattern.DOTALL);
        Matcher m3 = updRegex2.matcher(data);
        int i = 0;
        while (m3.find()) {
            String content = m3.group(1).replace("(", "{").replace(")", "}");
            javaCode.append("    public static final int[][] TILE_COORDS_").append(i).append(" = {\n")
                    .append("        ").append(content.trim()).append("\n    };\n\n");
            i++;
        }

        javaCode.append("}\n");

        Files.write(Paths.get("c:\\Users\\agr_b\\sm-rando-java\\src\\main\\java\\com\\maprando\\patch\\MapTilesArrays.java"), javaCode.toString().getBytes());
        System.out.println("Done extracting arrays!");
    }
}
