package myandroid.app.hhobzic.inventorymobile.scanner.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import kotlin.UByte;
import kotlin.UShort;

public class StringUtils {
    private static String hexString = "0123456789ABCDEF";

    public static int TwoByte2Int(byte b, boolean z) {
        return z ? b : b & UByte.MAX_VALUE;
    }

    public static int byte2int(byte b, byte b2) {
        return (short) (((b & UByte.MAX_VALUE) << 8) | (b2 & UByte.MAX_VALUE));
    }

    public static int isOdd(int i) {
        return i & 1;
    }

    private static int toUnsigned(short s) {
        return s & UShort.MAX_VALUE;
    }

    public static String Byte2Hex(Byte b) {
        return String.format("%02x", new Object[]{b}).toUpperCase();
    }

    public static String ByteArrToHex(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte valueOf : bArr) {
            sb.append(Byte2Hex(Byte.valueOf(valueOf)));
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String ByteArrToHex(byte[] bArr, int i, int i2) {
        StringBuilder sb = new StringBuilder();
        while (i < i2) {
            sb.append(Byte2Hex(Byte.valueOf(bArr[i])));
            i++;
        }
        return sb.toString();
    }

    public static byte HexToByte(String str) {
        str.replace(" ", "");
        return (byte) Integer.parseInt(str, 16);
    }

    public static byte[] HexToByteArr(String str) {
        byte[] bArr;
        int length = str.length();
        if (isOdd(length) != 1) {
            bArr = new byte[(length / 2)];
        } else {
            length++;
            bArr = new byte[(length / 2)];
            str = "0" + str;
        }
        int i = 0;
        int i2 = 0;
        while (i < length) {
            int i3 = i + 2;
            bArr[i2] = HexToByte(str.substring(i, i3));
            i2++;
            i = i3;
        }
        return bArr;
    }

    public static int HexToInt(String str) {
        return Integer.parseInt(str, 16);
    }

    public static byte[] toByteArray(int i, int i2) {
        byte[] bArr = new byte[i2];
        int i3 = 0;
        while (i3 < 4 && i3 < i2) {
            bArr[i3] = (byte) ((i >> (i3 * 8)) & 255);
            i3++;
        }
        return bArr;
    }

    public static int[] ByteArray2IntArray(byte[] bArr) {
        int[] iArr = new int[(bArr.length / 2)];
        int i = 0;
        for (int i2 = 0; i2 < bArr.length; i2 += 2) {
            int i3 = i2 + 1;
            if (i3 < bArr.length) {
                iArr[i] = byte2int(bArr[i2], bArr[i3]);
                i++;
            }
        }
        return iArr;
    }

    public static int byte2intUnsigned(byte b, byte b2) {
        return toUnsigned((short) (((b & UByte.MAX_VALUE) << 8) | (b2 & UByte.MAX_VALUE)));
    }

    public static String decode(String str) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(str.length() / 2);
        for (int i = 0; i < str.length(); i += 2) {
            byteArrayOutputStream.write((hexString.indexOf(str.charAt(i)) << 4) | hexString.indexOf(str.charAt(i + 1)));
        }
        return new String(byteArrayOutputStream.toByteArray());
    }

    public static String binary(byte[] bArr, int i) {
        return new BigInteger(1, bArr).toString(2);
    }

    public static String binary2(byte[] bArr) {
        String str = "";
        for (byte binaryString : bArr) {
            String binaryString2 = Integer.toBinaryString(binaryString);
            if (binaryString2.length() > 8) {
                binaryString2 = binaryString2.substring(binaryString2.length() - 8);
            } else if (binaryString2.length() < 8) {
                binaryString2 = "00000000".substring(binaryString2.length()) + binaryString2;
            }
            str = str + binaryString2;
        }
        return str;
    }

    public static String binary2(byte b) {
        String binaryString = Integer.toBinaryString(b);
        if (binaryString.length() > 8) {
            binaryString = binaryString.substring(binaryString.length() - 8);
        } else if (binaryString.length() < 8) {
            binaryString = "00000000".substring(binaryString.length()) + binaryString;
        }
        return "" + binaryString;
    }

    public static String binaryToHex(String str) {
        String hexString2 = Long.toHexString(Long.parseLong(str, 2));
        return hexString2.length() % 2 == 1 ? "0" + hexString2 : hexString2;
    }

    public static byte bit2byte(String str) {
        int length = str.length() - 1;
        byte b = 0;
        int i = 0;
        while (length >= 0) {
            b = (byte) ((int) (((double) b) + (((double) Byte.parseByte(str.charAt(length) + "")) * Math.pow(2.0d, (double) i))));
            length--;
            i++;
        }
        return b;
    }

    public static String replaceAll(String str, String str2, String str3) {
        return str.replaceAll(str2, str3);
    }
}
