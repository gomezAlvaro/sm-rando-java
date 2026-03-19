package com.maprando.patch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IpsPatch {

    public record Chunk(int start, int end) {}

    private static Chunk getNextChunk(int pos, byte[] oldRom, byte[] newRom) {
        while (pos < oldRom.length && pos < newRom.length) {
            if (oldRom[pos] != newRom[pos]) {
                break;
            }
            pos++;
        }
        
        if (pos == oldRom.length || pos == newRom.length) {
            return new Chunk(oldRom.length, newRom.length);
        }

        int start = pos;
        while (pos < oldRom.length && pos < newRom.length) {
            if (oldRom[pos] == newRom[pos]) {
                break;
            }
            pos++;
        }
        
        if (pos == oldRom.length || pos == newRom.length) {
            return new Chunk(start, newRom.length);
        }
        return new Chunk(start, pos);
    }

    private static void pushSplitChunks(List<Chunk> chunkVec, Chunk chunk) {
        int start = chunk.start();
        while (start + 0xFFFF < chunk.end()) {
            chunkVec.add(new Chunk(start, start + 0xFFFF));
            start += 0xFFFF;
        }
        chunkVec.add(new Chunk(start, chunk.end()));
    }

    private static List<Chunk> getChunks(byte[] oldRom, byte[] newRom) {
        List<Chunk> chunkVec = new ArrayList<>();
        int pos = 0;
        while (true) {
            Chunk chunk = getNextChunk(pos, oldRom, newRom);
            if (chunk.start() != chunk.end()) {
                pushSplitChunks(chunkVec, chunk);
            }
            if (chunk.end() >= newRom.length) {
                return chunkVec;
            }
            pos = chunk.end();
        }
    }

    public static byte[] createIpsPatch(byte[] oldRom, byte[] newRom) {
        if (newRom.length < oldRom.length) {
            throw new IllegalArgumentException("newRom must be large or equal to oldRom");
        }
        
        List<Chunk> chunks = getChunks(oldRom, newRom);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("PATCH".getBytes());
            for (Chunk chunk : chunks) {
                int start = chunk.start();
                out.write((start >> 16) & 0xFF);
                out.write((start >> 8) & 0xFF);
                out.write(start & 0xFF);
                
                int size = chunk.end() - chunk.start();
                if (size > 0xFFFF || size <= 0) {
                    throw new IllegalStateException("Split logic failed");
                }
                out.write((size >> 8) & 0xFF);
                out.write(size & 0xFF);
                
                out.write(newRom, chunk.start(), size);
            }
            out.write("EOF".getBytes());
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error writing IPS patch", e);
        }
    }
}
