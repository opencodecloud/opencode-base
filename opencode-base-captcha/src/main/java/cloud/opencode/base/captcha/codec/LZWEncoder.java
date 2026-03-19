package cloud.opencode.base.captcha.codec;

import java.io.IOException;
import java.io.OutputStream;

/**
 * LZW Encoder - Implements LZW compression for GIF
 * LZW 编码器 - 实现 GIF 的 LZW 压缩
 *
 * <p>This class implements the LZW compression algorithm used in GIF encoding.</p>
 * <p>此类实现用于 GIF 编码的 LZW 压缩算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LZW compression for GIF pixel data - GIF 像素数据的 LZW 压缩</li>
 *   <li>Variable bit-width encoding with automatic code table expansion - 可变位宽编码及自动代码表扩展</li>
 *   <li>Block output buffering for GIF sub-block format - GIF 子块格式的块输出缓冲</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LZWEncoder encoder = new LZWEncoder(width, height, pixels, colorDepth);
 * encoder.encode(outputStream);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (instance has mutable state) - 线程安全: 否（实例包含可变状态）</li>
 *   <li>Null-safe: No (parameters must be non-null) - 空值安全: 否（参数不能为空）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the number of pixels - 时间复杂度: O(n)，n 为像素数</li>
 *   <li>Space complexity: O(1) fixed-size hash and code tables - 空间复杂度: O(1) 固定大小的哈希和编码表</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
final class LZWEncoder {

    private static final int EOF = -1;
    private static final int BITS = 12;
    private static final int HSIZE = 5003;

    private final int imgW;
    private final int imgH;
    private final byte[] pixAry;
    private final int initCodeSize;
    private int remaining;
    private int curPixel;

    // GIF compression state
    private int nBits;
    private int maxbits = BITS;
    private int maxcode;
    private int maxmaxcode = 1 << BITS;

    private final int[] htab = new int[HSIZE];
    private final int[] codetab = new int[HSIZE];

    private int hsize = HSIZE;

    private int freeEnt = 0;
    private boolean clearFlg = false;

    private int gInitBits;
    private int clearCode;
    private int eofCode;

    // Output buffer
    private int curAccum = 0;
    private int curBits = 0;
    private final int[] masks = {
        0x0000, 0x0001, 0x0003, 0x0007, 0x000F,
        0x001F, 0x003F, 0x007F, 0x00FF, 0x01FF,
        0x01FF, 0x03FF, 0x07FF, 0x0FFF, 0x1FFF,
        0x3FFF, 0x7FFF, 0xFFFF
    };

    private int aCount;
    private final byte[] accum = new byte[256];

    LZWEncoder(int width, int height, byte[] pixels, int colorDepth) {
        this.imgW = width;
        this.imgH = height;
        this.pixAry = pixels;
        this.initCodeSize = Math.max(2, colorDepth);
    }

    void encode(OutputStream os) throws IOException {
        os.write(initCodeSize);
        remaining = imgW * imgH;
        curPixel = 0;
        compress(initCodeSize + 1, os);
        os.write(0);
    }

    private void compress(int initBits, OutputStream outs) throws IOException {
        int fcode;
        int i;
        int c;
        int ent;
        int disp;
        int hsizeReg;
        int hshift;

        gInitBits = initBits;
        clearFlg = false;
        nBits = gInitBits;
        maxcode = maxCode(nBits);

        clearCode = 1 << (initBits - 1);
        eofCode = clearCode + 1;
        freeEnt = clearCode + 2;

        aCount = 0;

        ent = nextPixel();

        hshift = 0;
        for (fcode = hsize; fcode < 65536; fcode *= 2) {
            ++hshift;
        }
        hshift = 8 - hshift;

        hsizeReg = hsize;
        clHash(hsizeReg);

        output(clearCode, outs);

        outer_loop:
        while ((c = nextPixel()) != EOF) {
            fcode = (c << maxbits) + ent;
            i = (c << hshift) ^ ent;

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) {
                disp = hsizeReg - i;
                if (i == 0) {
                    disp = 1;
                }
                do {
                    if ((i -= disp) < 0) {
                        i += hsizeReg;
                    }
                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }
            output(ent, outs);
            ent = c;
            if (freeEnt < maxmaxcode) {
                codetab[i] = freeEnt++;
                htab[i] = fcode;
            } else {
                clBlock(outs);
            }
        }
        output(ent, outs);
        output(eofCode, outs);
    }

    private void clBlock(OutputStream outs) throws IOException {
        clHash(hsize);
        freeEnt = clearCode + 2;
        clearFlg = true;
        output(clearCode, outs);
    }

    private void clHash(int hsize) {
        for (int i = 0; i < hsize; ++i) {
            htab[i] = -1;
        }
    }

    private int maxCode(int nBits) {
        return (1 << nBits) - 1;
    }

    private int nextPixel() {
        if (remaining == 0) {
            return EOF;
        }
        --remaining;
        return pixAry[curPixel++] & 0xff;
    }

    private void output(int code, OutputStream outs) throws IOException {
        curAccum &= masks[curBits];

        if (curBits > 0) {
            curAccum |= (code << curBits);
        } else {
            curAccum = code;
        }

        curBits += nBits;

        while (curBits >= 8) {
            charOut((byte) (curAccum & 0xff), outs);
            curAccum >>= 8;
            curBits -= 8;
        }

        if (freeEnt > maxcode || clearFlg) {
            if (clearFlg) {
                maxcode = maxCode(nBits = gInitBits);
                clearFlg = false;
            } else {
                ++nBits;
                if (nBits == maxbits) {
                    maxcode = maxmaxcode;
                } else {
                    maxcode = maxCode(nBits);
                }
            }
        }

        if (code == eofCode) {
            while (curBits > 0) {
                charOut((byte) (curAccum & 0xff), outs);
                curAccum >>= 8;
                curBits -= 8;
            }
            flushChar(outs);
        }
    }

    private void charOut(byte c, OutputStream outs) throws IOException {
        accum[aCount++] = c;
        if (aCount >= 254) {
            flushChar(outs);
        }
    }

    private void flushChar(OutputStream outs) throws IOException {
        if (aCount > 0) {
            outs.write(aCount);
            outs.write(accum, 0, aCount);
            aCount = 0;
        }
    }
}
