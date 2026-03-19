package com.maprando.patch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rom {
    public byte[] data;
    public boolean trackTouched;
    public final Set<Integer> touched = new HashSet<>();

    public Rom(byte[] data) {
        this.data = data;
        this.trackTouched = false;
    }

    public void expand(int size) {
        if (data.length < size) {
            byte[] newData = new byte[size];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    public static int snes2pc(int addr) {
        return ((addr >> 1) & 0x3F8000) | (addr & 0x7FFF);
    }

    public static int pc2snes(int addr) {
        return ((addr << 1) & 0xFF0000) | (addr & 0xFFFF) | 0x808000;
    }

    public void enableTracking() {
        this.trackTouched = true;
        this.touched.clear();
    }

    public static Rom load(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        if (data.length == 3146240) {
            byte[] unheadered = new byte[3145728];
            System.arraycopy(data, 512, unheadered, 0, 3145728);
            data = unheadered;
        } else if (data.length != 3145728) {
            throw new IllegalArgumentException("Invalid Super Metroid ROM size. Expected 3145728 bytes (unheadered) or 3146240 bytes (headered). Actual: " + data.length);
        }
        return new Rom(data);
    }

    public void save(Path path) throws IOException {
        Files.write(path, data);
    }

    public int readU8(int addr) {
        if (addr >= data.length) throw new IndexOutOfBoundsException("readU8 addr out of bounds: " + addr);
        return data[addr] & 0xFF;
    }

    public int readU16(int addr) {
        if (addr + 1 >= data.length) throw new IndexOutOfBoundsException("readU16 addr out of bounds: " + addr);
        return (data[addr] & 0xFF) | ((data[addr + 1] & 0xFF) << 8);
    }

    public int readU24(int addr) {
        if (addr + 2 >= data.length) throw new IndexOutOfBoundsException("readU24 addr out of bounds: " + addr);
        return (data[addr] & 0xFF) | ((data[addr + 1] & 0xFF) << 8) | ((data[addr + 2] & 0xFF) << 16);
    }

    public byte[] readN(int addr, int n) {
        if (addr + n > data.length) throw new IndexOutOfBoundsException("readN addr out of bounds: " + addr);
        byte[] out = new byte[n];
        System.arraycopy(data, addr, out, 0, n);
        return out;
    }

    public void writeU8(int addr, int x) {
        if (addr >= data.length) throw new IndexOutOfBoundsException("writeU8 addr out of bounds: " + addr);
        if (x < 0 || x > 0xFF) throw new IllegalArgumentException("writeU8 data does not fit: " + x);
        data[addr] = (byte) x;
        if (trackTouched) {
            touched.add(addr);
        }
    }

    public void writeU16(int addr, int x) {
        if (addr + 1 >= data.length) throw new IndexOutOfBoundsException("writeU16 addr out of bounds: " + addr);
        if (x < 0 || x > 0xFFFF) throw new IllegalArgumentException("writeU16 data does not fit: " + x);
        writeU8(addr, x & 0xFF);
        writeU8(addr + 1, x >> 8);
    }

    public void writeU24(int addr, int x) {
        if (addr + 2 >= data.length) throw new IndexOutOfBoundsException("writeU24 addr out of bounds: " + addr);
        if (x < 0 || x > 0xFFFFFF) throw new IllegalArgumentException("writeU24 data does not fit: " + x);
        writeU8(addr, x & 0xFF);
        writeU8(addr + 1, (x >> 8) & 0xFF);
        writeU8(addr + 2, (x >> 16) & 0xFF);
    }

    public void writeN(int addr, byte[] x) {
        if (addr + x.length > data.length) throw new IndexOutOfBoundsException("writeN addr out of bounds: " + addr);
        for (int i = 0; i < x.length; i++) {
            writeU8(addr + i, x[i] & 0xFF);
        }
    }

    public record Range(int start, int end) {}

    public List<Range> getModifiedRanges() {
        List<Integer> addresses = new ArrayList<>(touched);
        Collections.sort(addresses);
        List<Range> ranges = new ArrayList<>();

        int i = 0;
        outer:
        while (i < addresses.size()) {
            for (int j = i; j < addresses.size() - 1; j++) {
                if (addresses.get(j + 1) != addresses.get(j) + 1) {
                    ranges.add(new Range(addresses.get(i), addresses.get(j) + 1));
                    i = j + 1;
                    continue outer;
                }
            }
            ranges.add(new Range(addresses.get(i), addresses.get(addresses.size() - 1) + 1));
            break;
        }

        int totalLen = ranges.stream().mapToInt(r -> r.end - r.start).sum();
        if (totalLen != addresses.size()) {
            throw new IllegalStateException("Ranges total length does not match addresses sum!");
        }

        return ranges;
    }
}
