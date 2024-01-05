package org.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Decompressor {

    public void decompress(String compressedFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(compressedFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Map<String, byte[]> codeWords = new HashMap<>();

        File inputFile = new File(compressedFilePath);
        String parentDirectory = inputFile.getParent();
        String fileName = inputFile.getName();
        String baseName = fileName.replaceFirst("[.][^.]+$", ""); // Remove the extension
        String outputPath = parentDirectory + File.separator + "extracted." + baseName;
        outputPath = outputPath.replace(".hc", "");


        // Read and parse the header
        parseHeader(bis, codeWords);

        // Decompress the data
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            decodeData(bis, bos, codeWords);
        }

        bis.close();
    }

    private void parseHeader(BufferedInputStream bis, Map<String, byte[]> codeWords) throws IOException {
        while (true) {
            int keyLength = bis.read(); // Length of the key
            if (keyLength == 0) break;  // End-of-header marker

            byte[] keyBytes = new byte[keyLength];
            bis.read(keyBytes); // Read the key

            int codeWordLength = bis.read(); // Length of the codeword
            byte[] codeWordBits = new byte[(codeWordLength + 7) / 8];
            bis.read(codeWordBits); // Read codeword bits
            String codeword = binaryBytesToString(codeWordBits, codeWordLength);

            codeWords.put(codeword, keyBytes);
        }
    }

    void decodeData(BufferedInputStream bis, BufferedOutputStream bos, Map<String, byte[]> codeWords) throws IOException {
        long startTime = System.nanoTime();

        int chunkSize = 16384;
        byte[] dataBuffer = new byte[chunkSize];
        StringBuilder bitStringBuilder = new StringBuilder();
        int maxCodeLength = getMaxCodeLength(codeWords);

        int nRead;
        while ((nRead = bis.read(dataBuffer, 0, dataBuffer.length)) != -1) {
            for (int byteIndex = 0; byteIndex < nRead; byteIndex++) {
                bitStringBuilder.append(byteToBinaryString(dataBuffer[byteIndex]));

                while (bitStringBuilder.length() >= maxCodeLength) {
                    String currentCode = extractCode(bitStringBuilder, codeWords, maxCodeLength);
                    if (currentCode != null) {
                        bos.write(codeWords.get(currentCode));
                    }
                }
            }
        }

        // Process remaining bits
        String remainingCode = extractCode(bitStringBuilder, codeWords, maxCodeLength);
        if (remainingCode != null && !remainingCode.isEmpty()) {
            bos.write(codeWords.get(remainingCode));
        }

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.println(elapsedTimeInSeconds);
    }

    private int getMaxCodeLength(Map<String, byte[]> codeWords) {
        int maxLength = 0;
        for (String code : codeWords.keySet()) {
            if (code.length() > maxLength) {
                maxLength = code.length();
            }
        }
        return maxLength;
    }

    private String extractCode(StringBuilder bitStringBuilder, Map<String, byte[]> codeWords, int maxCodeLength) {
        for (int length = 1; length <= maxCodeLength && length <= bitStringBuilder.length(); length++) {
            String candidate = bitStringBuilder.substring(0, length);
            if (codeWords.containsKey(candidate)) {
                bitStringBuilder.delete(0, length);
                return candidate;
            }
        }
        return null;
    }

    private String byteToBinaryString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    private String binaryBytesToString(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if ((bytes[i / 8] & (1 << (7 - i % 8))) != 0) {
                sb.append('1');
            } else {
                sb.append('0');
            }
        }
        return sb.toString();
    }
}
