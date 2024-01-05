package org.example;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Compressor {

    public void compress(String filePath, int n) throws IOException {
        long startTime = System.nanoTime();
        File file = new File(filePath);
        String fileName = file.getName();
        String outputPath = file.getParent() + File.separator + "20010605." + n + "." + fileName + ".hc";

        Map<ByteArrayWrapper, Integer> frequencies = getFrequencyMap(filePath, n);
        HuffmanTree tree = new HuffmanTree();

        Node root = tree.getTree(frequencies);
        Map<ByteArrayWrapper, String> codeWords = tree.getCodeWords(root);

        // Original file size
        long originalSize = file.length();

        compressFile(filePath, outputPath, codeWords, n);

        // Compressed file size
        File compressedFile = new File(outputPath);
        long compressedSize = compressedFile.length();

        // Calculate and print the compression ratio
        double compressionRatio = (double) compressedSize / originalSize;
        System.out.println(compressionRatio);

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.println(elapsedTimeInSeconds);
    }

    private Map<ByteArrayWrapper, Integer> getFrequencyMap(String filePath, int n) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] fileData = new byte[(int) file.length()];
        fis.read(fileData);
        fis.close();

        Map<ByteArrayWrapper, Integer> frequencies = new HashMap<>();
        for (int i = 0; i <= fileData.length - n; i += n) {
            // Handle the case where the remaining data is less than n bytes
            int end = Math.min(i + n, fileData.length); // Handles Padding
            byte[] key = Arrays.copyOfRange(fileData, i, end);
            frequencies.put(new ByteArrayWrapper(key), frequencies.getOrDefault(new ByteArrayWrapper(key), 0) + 1);
        }

        return frequencies;
    }

    private void compressFile(String in, String out, Map<ByteArrayWrapper, String> codeWords, int n) throws IOException {
        File inputFile = new File(in);
        FileInputStream fis = new FileInputStream(inputFile);

        try (BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(out))) {
            fout.write(convertCodewordsToBytes(codeWords)); // Write header

            byte[] buffer = new byte[16384 - 16384 % n]; // Chunk size
            int bytesRead;
            StringBuilder bitStringBuilder = new StringBuilder();

            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i += n) {
                    int end = Math.min(i + n, bytesRead);
                    byte[] key = Arrays.copyOfRange(buffer, i, end);
                    ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
                    String codeword = codeWords.get(keyWrapper);

                    if (codeword != null) {
                        bitStringBuilder.append(codeword);
                    }

                    // Write bytes when there are enough bits in the StringBuilder
                    while (bitStringBuilder.length() >= 8) {
                        int byteValue = Integer.parseInt(bitStringBuilder.substring(0, 8), 2);
                        fout.write(byteValue);
                        bitStringBuilder.delete(0, 8);
                    }
                }
            }

            // Handle any remaining bits
            if (!bitStringBuilder.isEmpty()) {
                int paddingLength = 8 - bitStringBuilder.length();
                for (int i = 0; i < paddingLength; i++) {
                    bitStringBuilder.append('0');
                }
                int byteValue = Integer.parseInt(bitStringBuilder.toString(), 2);
                fout.write(byteValue);
            }
        } finally {
            fis.close();
        }
    }


    private byte[] convertCodewordsToBytes(Map<ByteArrayWrapper, String> codewords) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Map.Entry<ByteArrayWrapper, String> entry : codewords.entrySet()) {
            byte[] keyBytes = entry.getKey().getContent();
            baos.write(keyBytes.length); // Write the length of the key
            baos.write(keyBytes); // Write the key itself

            String codeword = entry.getValue();
            baos.write(codeword.length()); // Write the length of the codeword

            byte[] codewordBytes = stringToBinary(codeword); // Convert codeword to binary
            baos.write(codewordBytes, 0, (codeword.length() + 7) / 8); // Write the codeword
        }

        baos.write(0); // End-of-header marker
        return baos.toByteArray();
    }

    private byte[] stringToBinary(String binaryString) {
        int byteLength = (binaryString.length() + 7) / 8;
        byte[] bytes = new byte[byteLength];
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                bytes[i / 8] |= (byte) (1 << (7 - i % 8));
            }
        }
        return bytes;
    }

}