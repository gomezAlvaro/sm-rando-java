package com.maprando.patch;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Compression {

    private static final int BLOCK_TYPE_RAW = 0;
    private static final int BLOCK_TYPE_BYTE_RLE = 1;

    private static void encodeBlockHeader(int size, int blockType, ByteArrayOutputStream out) {
        int size1 = size - 1;
        if (size1 <= 31) {
            out.write(size1 | (blockType << 5));
        } else {
            out.write(0xE0 | (blockType << 2) | ((size1 >> 8) & 0xFF));
            out.write(size1 & 0xFF);
        }
    }

    private static void encodeRawBlock(byte[] data, ByteArrayOutputStream out) {
        int count = (data.length + 1023) / 1024;
        for (int i = 0; i < count; i++) {
            int start = i * 1024;
            int end = Math.min((i + 1) * 1024, data.length);
            int size = end - start;
            encodeBlockHeader(size, BLOCK_TYPE_RAW, out);
            out.write(data, start, size);
        }
    }

    private static void encodeRleBlock(byte value, int count, ByteArrayOutputStream out) {
        int blocks = (count + 1023) / 1024;
        for (int i = 0; i < blocks; i++) {
            int end = Math.min((i + 1) * 1024, count);
            int size = end - i * 1024;
            encodeBlockHeader(size, BLOCK_TYPE_BYTE_RLE, out);
            out.write(value);
        }
    }

    private static void encodeBlock(ByteArrayOutputStream blockData, byte value, int rleCount, ByteArrayOutputStream out) {
        byte[] data = blockData.toByteArray();
        if (data.length > 0) {
            encodeRawBlock(data, out);
            blockData.reset();
        }
        if (rleCount > 0) {
            encodeRleBlock(value, rleCount, out);
        }
    }

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream blockData = new ByteArrayOutputStream();
        int prev = -1;
        int rleCount = 0;

        for (byte b : data) {
            int x = b & 0xFF;
            if (x == prev) {
                rleCount++;
            } else {
                if (rleCount >= 3) {
                    encodeBlock(blockData, (byte) prev, rleCount, out);
                } else {
                    for (int i = 0; i < rleCount; i++) {
                        blockData.write(prev);
                    }
                }
                rleCount = 1;
                prev = x;
            }
        }
        encodeBlock(blockData, (byte) prev, rleCount, out);
        out.write(0xFF);
        return out.toByteArray();
    }

    public static byte[] decompress(Rom rom, int startAddr) {
        List<Byte> out = new ArrayList<>();
        int addr = startAddr;

        while (true) {
            int b = rom.readU8(addr++);
            if (b == 0xFF) {
                byte[] res = new byte[out.size()];
                for (int i = 0; i < res.length; i++) res[i] = out.get(i);
                return res;
            }

            int blockType = b >> 5;
            int size;

            if (blockType != 7) {
                size = (b & 0x1F) + 1;
            } else {
                int bNext = rom.readU8(addr++);
                size = (((b & 3) << 8) | bNext) + 1;
                blockType = (b >> 2) & 7;
            }

            switch (blockType) {
                case 0: { // Raw
                    byte[] raw = rom.readN(addr, size);
                    addr += size;
                    for (byte r : raw) out.add(r);
                    break;
                }
                case 1: { // Byte RLE
                    byte val = (byte) rom.readU8(addr++);
                    for (int i = 0; i < size; i++) out.add(val);
                    break;
                }
                case 2: { // Word RLE
                    byte b0 = (byte) rom.readU8(addr++);
                    byte b1 = (byte) rom.readU8(addr++);
                    for (int i = 0; i < (size >> 1); i++) {
                        out.add(b0);
                        out.add(b1);
                    }
                    if ((size & 1) == 1) {
                        out.add(b0);
                    }
                    break;
                }
                case 3: { // Incrementing
                    byte val = (byte) rom.readU8(addr++);
                    for (int i = 0; i < size; i++) {
                        out.add(val);
                        val++;
                    }
                    break;
                }
                case 4: { // Copy absolute
                    int offset = rom.readU16(addr);
                    addr += 2;
                    if (offset >= out.size()) throw new IllegalStateException();
                    for (int i = offset; i < offset + size; i++) out.add(out.get(i));
                    break;
                }
                case 5: { // Copy absolute complement
                    int offset = rom.readU16(addr);
                    addr += 2;
                    if (offset >= out.size()) throw new IllegalStateException();
                    for (int i = offset; i < offset + size; i++) out.add((byte)(out.get(i) ^ 0xFF));
                    break;
                }
                case 6: { // Copy relative
                    int rel = rom.readU8(addr++);
                    if (rel > out.size()) throw new IllegalStateException();
                    int offset = out.size() - rel;
                    for (int i = offset; i < offset + size; i++) out.add(out.get(i));
                    break;
                }
                case 7: { // Copy relative complement
                    int rel = rom.readU8(addr++);
                    if (rel > out.size()) throw new IllegalStateException();
                    int offset = out.size() - rel;
                    for (int i = offset; i < offset + size; i++) out.add((byte)(out.get(i) ^ 0xFF));
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown block type: " + blockType);
            }
        }
    }
}
