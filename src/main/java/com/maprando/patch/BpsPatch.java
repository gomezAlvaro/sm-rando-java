package com.maprando.patch;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

public class BpsPatch {

    private static final int SOURCE_MATCH_THRESHOLD = 4;

    private static abstract class BpsBlock {
        public static class Unchanged extends BpsBlock {}
        public static class SourceCopy extends BpsBlock {
            int srcStart;
            int dstStart;
            int length;
            SourceCopy(int srcStart, int dstStart, int length) {
                this.srcStart = srcStart;
                this.dstStart = dstStart;
                this.length = length;
            }
        }
        public static class TargetCopy extends BpsBlock {
            int srcStart;
            int dstStart;
            int length;
            TargetCopy(int srcStart, int dstStart, int length) {
                this.srcStart = srcStart;
                this.dstStart = dstStart;
                this.length = length;
            }
        }
        public static class Data extends BpsBlock {
            int dstStart;
            byte[] data;
            Data(int dstStart, byte[] data) {
                this.dstStart = dstStart;
                this.data = data;
            }
        }
    }

    private final List<BpsBlock> blocks;

    private BpsPatch(List<BpsBlock> blocks) {
        this.blocks = blocks;
    }

    public static BpsPatch decode(byte[] data) {
        BpsDecoder decoder = new BpsDecoder(data);
        decoder.decode();
        return new BpsPatch(decoder.blocks);
    }

    public void apply(byte[] source, byte[] output) {
        for (BpsBlock block : blocks) {
            if (block instanceof BpsBlock.Unchanged) {
                // Do nothing
            } else if (block instanceof BpsBlock.SourceCopy sc) {
                System.arraycopy(source, sc.srcStart, output, sc.dstStart, sc.length);
            } else if (block instanceof BpsBlock.TargetCopy tc) {
                for (int i = 0; i < tc.length; i++) {
                    output[tc.dstStart + i] = output[tc.srcStart + i];
                }
            } else if (block instanceof BpsBlock.Data d) {
                System.arraycopy(d.data, 0, output, d.dstStart, d.data.length);
            }
        }
    }

    private static class BpsDecoder {
        byte[] patchBytes;
        int patchPos = 0;
        int outputPos = 0;
        int srcPos = 0;
        int dstPos = 0;
        List<BpsBlock> blocks = new ArrayList<>();

        BpsDecoder(byte[] patchBytes) {
            this.patchBytes = patchBytes;
        }

        void decode() {
            byte[] magic = readN(4);
            if (!Arrays.equals(magic, "BPS1".getBytes())) {
                throw new IllegalArgumentException("Invalid BPS magic prefix");
            }

            decodeNumber(); // src_size
            decodeNumber(); // dst_size
            int metadataSize = decodeNumber();
            patchPos += metadataSize;

            while (patchPos < patchBytes.length - 12) {
                BpsBlock block = decodeBlock();
                if (!(block instanceof BpsBlock.Unchanged)) {
                    blocks.add(block);
                }
            }

            if (patchPos != patchBytes.length - 12) {
                throw new IllegalStateException("Patch length mismatch");
            }
        }

        byte read() {
            if (patchPos >= patchBytes.length) {
                throw new IllegalStateException("BPS read past end of data");
            }
            return patchBytes[patchPos++];
        }

        byte[] readN(int n) {
            byte[] out = new byte[n];
            System.arraycopy(patchBytes, patchPos, out, 0, n);
            patchPos += n;
            return out;
        }

        BpsBlock decodeBlock() {
            int cmd = decodeNumber();
            int action = cmd & 3;
            int length = (cmd >> 2) + 1;

            switch (action) {
                case 0:
                    outputPos += length;
                    return new BpsBlock.Unchanged();
                case 1:
                    BpsBlock block1 = new BpsBlock.Data(outputPos, readN(length));
                    outputPos += length;
                    return block1;
                case 2:
                    int rawOffsetSrc = decodeNumber();
                    boolean srcNeg = (rawOffsetSrc & 1) == 1;
                    int srcAbs = rawOffsetSrc >> 1;
                    int offsetSrc = srcNeg ? -srcAbs : srcAbs;
                    srcPos = srcPos + offsetSrc;
                    BpsBlock block2 = new BpsBlock.SourceCopy(srcPos, outputPos, length);
                    srcPos += length;
                    outputPos += length;
                    return block2;
                case 3:
                    int rawOffsetDst = decodeNumber();
                    boolean dstNeg = (rawOffsetDst & 1) == 1;
                    int dstAbs = rawOffsetDst >> 1;
                    int offsetDst = dstNeg ? -dstAbs : dstAbs;
                    dstPos = dstPos + offsetDst;
                    BpsBlock block3 = new BpsBlock.TargetCopy(dstPos, outputPos, length);
                    dstPos += length;
                    outputPos += length;
                    return block3;
                default:
                    throw new IllegalStateException("Unexpected BPS action: " + action);
            }
        }

        int decodeNumber() {
            int out = 0;
            int shift = 1;
            for (int i = 0; i < 10; i++) {
                byte x = read();
                out += (x & 0x7f) * shift;
                if ((x & 0x80) != 0) {
                    return out;
                }
                shift <<= 7;
                out += shift;
            }
            throw new IllegalStateException("invalid BPS number encoding");
        }
    }

    public static class BpsEncoder {
        private final SuffixTree sourceSuffixTree;
        private final byte[] target;
        private final List<int[]> modifiedRanges;
        
        private final ByteArrayOutputStream patchBytes = new ByteArrayOutputStream();
        private int srcPos = 0;
        private int inputPos = 0;

        public BpsEncoder(SuffixTree sourceSuffixTree, byte[] target, List<int[]> modifiedRanges) {
            this.sourceSuffixTree = sourceSuffixTree;
            this.target = target;
            this.modifiedRanges = modifiedRanges;
        }

        private static long computeCrc32(byte[] data) {
            CRC32 crc = new CRC32();
            crc.update(data);
            return crc.getValue();
        }

        private void writeCrc(long crc) {
            patchBytes.write((int) (crc & 0xFF));
            patchBytes.write((int) ((crc >> 8) & 0xFF));
            patchBytes.write((int) ((crc >> 16) & 0xFF));
            patchBytes.write((int) ((crc >> 24) & 0xFF));
        }

        public byte[] encode() {
            try {
                patchBytes.write("BPS1".getBytes());
                encodeNumber(sourceSuffixTree.data.length);
                encodeNumber(target.length);
                encodeNumber(0); // metadata size

                for (int[] r : modifiedRanges) {
                    encodeRange(r[0], r[1]);
                }

                if (inputPos < sourceSuffixTree.data.length) {
                    encodeUnchanged(sourceSuffixTree.data.length - inputPos);
                }

                writeCrc(computeCrc32(sourceSuffixTree.data));
                writeCrc(computeCrc32(target));

                byte[] patchSoFar = patchBytes.toByteArray();
                writeCrc(computeCrc32(patchSoFar));
                
                return patchBytes.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Error encoding BPS", e);
            }
        }

        private void encodeRange(int startAddr, int endAddr) {
            if (inputPos < startAddr) {
                int length = startAddr - inputPos;
                encodeUnchanged(length);
                inputPos += length;
            }

            while (startAddr < endAddr) {
                SuffixTree.MatchResult match = sourceSuffixTree.findLongestPrefix(target, startAddr, endAddr);
                int sourceStart = match.start();
                int matchLength = match.matchLength();

                if (matchLength >= SOURCE_MATCH_THRESHOLD) {
                    if (startAddr > inputPos) {
                        encodeData(target, inputPos, startAddr);
                    }
                    encodeSourceCopy(sourceStart, matchLength);
                    inputPos = startAddr + matchLength;
                    startAddr += matchLength;
                } else {
                    startAddr++;
                }
            }

            if (endAddr > inputPos) {
                encodeData(target, inputPos, endAddr);
                inputPos = endAddr;
            }
        }

        private void encodeBlockHeader(int action, int length) {
            int x = action | ((length - 1) << 2);
            encodeNumber(x);
        }

        private void encodeUnchanged(int length) {
            encodeBlockHeader(0, length);
        }

        private void encodeData(byte[] data, int start, int end) {
            int length = end - start;
            encodeBlockHeader(1, length);
            for (int i = start; i < end; i++) {
                patchBytes.write(data[i]);
            }
        }

        private void encodeSourceCopy(int idx, int length) {
            encodeBlockHeader(2, length);
            int relativeIdx = idx - srcPos;
            if (relativeIdx < 0) {
                encodeNumber(1 | ((-relativeIdx) << 1));
            } else {
                encodeNumber(relativeIdx << 1);
            }
            srcPos = idx + length;
        }

        private void encodeNumber(int x) {
            for (int i = 0; i < 10; i++) {
                byte b = (byte) (x & 0x7F);
                x >>= 7;
                if (x == 0) {
                    patchBytes.write(0x80 | b);
                    break;
                }
                patchBytes.write(b);
                x -= 1;
            }
        }
    }
}
