import re

def process_file():
    with open(r'c:\Users\agr_b\MapRandomizer\rust\maprando\src\patch\map_tiles.rs', 'r') as f:
        data = f.read()

    java_code = """package com.maprando.patch;

public class MapTilesArrays {
"""

    # Extract [8][8] variables
    arr_regex = re.compile(r'let (\w+): \[\[u8; 8\]; 8\] = \[(.*?)\];', re.DOTALL)
    for name, content in arr_regex.findall(data):
        content = content.replace('[', '{').replace(']', '}')
        # Replace b, w constants with 15, 12
        content = re.sub(r'\bb\b', '15', content)
        content = re.sub(r'\bw\b', '12', content)
        java_code += f"    public static final int[][] {name.upper()} = {{\n"
        java_code += f"        {content.strip()}\n"
        java_code += f"    }};\n\n"

    # Extract apply_heat data (Slopes)
    heat_regex = re.compile(r'MapTileSpecialType::(\w+) => \{\s*data = apply_heat\(\[\s*(.*?)\s*\]\);\s*\}', re.DOTALL)
    for name, content in heat_regex.findall(data):
        name_upper = re.sub(r'(?<!^)(?=[A-Z])', '_', name).upper()
        content = content.replace('[', '{').replace(']', '}')
        java_code += f"    public static final int[][] {name_upper} = {{\n"
        java_code += f"        {content.strip()}\n"
        java_code += f"    }};\n\n"

    # Save stations / Refill stations vector coordinates update_tile
    upd_regex = re.compile(r'MapTileInterior::(\w+) => \{\s*update_tile\(\s*&mut data,\s*\d+,\s*&vec!\[(.*?)\]\s*,?\s*\);\s*\}', re.DOTALL)
    for name, content in upd_regex.findall(data):
        name_upper = re.sub(r'(?<!^)(?=[A-Z])', '_', name).upper()
        content = content.replace('(', '{').replace(')', '}')
        java_code += f"    public static final int[][] {name_upper}_COORDS = {{\n"
        java_code += f"        {content.strip()}\n"
        java_code += f"    }};\n\n"

    # There's also some with if-else
    upd_regex2 = re.compile(r'update_tile\(\s*&mut data,\s*\d+,\s*&vec!\[(.*?)\]\s*,?\s*\);', re.DOTALL)
    i = 0
    for content in upd_regex2.findall(data):
        content = content.replace('(', '{').replace(')', '}')
        java_code += f"    public static final int[][] TILE_COORDS_{i} = {{\n"
        java_code += f"        {content.strip()}\n"
        java_code += f"    }};\n\n"
        i += 1

    java_code += "}\n"

    with open(r'c:\Users\agr_b\sm-rando-java\src\main\java\com\maprando\patch\MapTilesArrays.java', 'w') as f:
        f.write(java_code)

process_file()
