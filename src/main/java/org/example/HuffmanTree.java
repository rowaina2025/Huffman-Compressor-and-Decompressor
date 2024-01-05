package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanTree {

    public Node getTree(Map<ByteArrayWrapper, Integer> frequencies) {
        PriorityQueue<Node> queue = new PriorityQueue<>();

        // initialize
        for(Map.Entry<ByteArrayWrapper, Integer> entry : frequencies.entrySet()) {
            byte[] key = entry.getKey().getContent(); int value = entry.getValue();
            queue.add(new Node(key, value));
        }

        // Build the Huffman tree
        while(queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            assert right != null;
            int combinedFreq = left.freq + right.freq;
            Node parent = new Node(null, combinedFreq);
            parent.left = left;
            parent.right = right;
            queue.add(parent);
        }
        return queue.poll();
    }

    public Map<ByteArrayWrapper, String> getCodeWords(Node root) {
        Map<ByteArrayWrapper, String> codewords = new HashMap<>();
        recurr(root, "", codewords);
        return codewords;
    }

    private void recurr(Node node, String code, Map<ByteArrayWrapper, String> codeWords) {
        if(node != null) {
            // Leaf node so assign the generated code word
            if(node.left == null && node.right == null && node.data != null) {
                codeWords.put(new ByteArrayWrapper(node.data), code);
            } else {
                // Build it recursively
                recurr(node.left, code + "0", codeWords);
                recurr(node.right, code + "1", codeWords);
            }
        }
    }


}
