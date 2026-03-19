import re
import os

rust_path = r'c:\Users\agr_b\MapRandomizer\rust\maprando\src\patch\map_tiles.rs'
out_path = r'c:\Users\agr_b\sm-rando-java\src\main\java\com\maprando\patch\MapTilesData.java'

with open(rust_path, 'r') as f:
    content = f.read()

# I will extract `draw_edge` coords, `get_gray_doors`, `get_objective_tiles` and `hazard_tiles` since they are huge.
java_code = """package com.maprando.patch;

import com.maprando.model.*;
import java.util.*;

public class MapTilesData {
"""

# Extract get_gray_doors
m = re.search(r'pub fn get_gray_doors\(\) -> Vec<\(RoomId, isize, isize, Direction\)> \{(.*?)\}', content, re.DOTALL)
if m:
    body = m.group(1)
    body = body.replace('use Direction::{Down, Left, Right, Up};', '')
    body = body.replace('vec![', 'Arrays.asList(').replace(']', ')')
    body = body.replace('(', 'new Object[]{').replace(')', '}')
    java_code += "    public static List<Object[]> getGrayDoors() {\n        return " + body.strip() + ";\n    }\n\n"

# We will just do a basic translation tool locally to help.
# Actually, the user doesn't need me to do clever python tricks if I can just write the Java code directly.
# Let's write the file and then call `python script.py`.
