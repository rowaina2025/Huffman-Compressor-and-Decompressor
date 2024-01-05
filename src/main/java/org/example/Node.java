package org.example;

// Node class
class Node implements Comparable<Node> {
    byte[] data;
    int freq;
    Node left, right;

    Node(byte[] data, int freq) {
        this.data = data;
        this.freq = freq;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.freq, other.freq);
    }
}