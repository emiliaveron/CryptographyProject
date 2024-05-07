package codeufaz.ikinci_kurs.Python.cryptography.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSAEncryption {

    public static void main(String[] args) {
        List<Integer> primes = generatePrimes(100);
        Random rand = new Random();
        int pIndex = rand.nextInt(primes.size());
        int qIndex = rand.nextInt(primes.size());
        int p = primes.get(pIndex);
        int q = primes.get(qIndex);
        int n = p * q;
        List<Integer> public_key = generatePublicKey(p, q);
        List<Integer> private_key = generatePrivateKey(p, q, public_key.get(0));
        String message = "Hello, RSA!";
        List<Integer> encoded = encode(message, public_key.get(0), n);
        System.out.println("Encoded message (public key): ");
        for (int num : encoded) {
            System.out.print(num + " ");
        }
        System.out.println();
        String decoded = decode(encoded, private_key.get(0), n);
        System.out.println("\nDecoded message (private key): ");
        System.out.println(decoded);
    }

    public static List<Integer> generatePrimes(int limit) {
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i < limit; i++) {
            boolean isPrime = true;
            for (int j = 2; j <= Math.sqrt(i); j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                primes.add(i);
            }
        }
        return primes;
    }

    public static List<Integer> generatePublicKey(int p, int q) {
        int n = p * q;
        List<Integer> public_key = new ArrayList<>();
        int e = 2;
        while (true) {
            if (gcd(e, (p - 1) * (q - 1)) == 1) {
                break;
            }
            e++;
        }
        public_key.add(e);
        public_key.add(n);
        return public_key;
    }

    public static List<Integer> generatePrivateKey(int p, int q, int e) {
        int n = p * q;
        List<Integer> private_key = new ArrayList<>();
        int d = 2;
        while (true) {
            if ((d * e) % ((p - 1) * (q - 1)) == 1) {
                break;
            }
            d++;
        }
        private_key.add(d);
        private_key.add(n);
        return private_key;
    }

    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public static List<Integer> encode(String message, int e, int n) {
        List<Integer> encoded = new ArrayList<>();
        for (char c : message.toCharArray()) {
            encoded.add(encrypt((int) c, e, n));
        }
        return encoded;
    }

    public static String decode(List<Integer> encoded, int d, int n) {
        StringBuilder decoded = new StringBuilder();
        for (int num : encoded) {
            decoded.append((char) decrypt(num, d, n));
        }
        return decoded.toString();
    }

    public static int encrypt(int message, int e, int n) {
        int encrypted_text = 1;
        while (e > 0) {
            encrypted_text *= message;
            encrypted_text %= n;
            e--;
        }
        return encrypted_text;
    }

    public static int decrypt(int encrypted_text, int d, int n) {
        int decrypted = 1;
        while (d > 0) {
            decrypted *= encrypted_text;
            decrypted %= n;
            d--;
        }
        return decrypted;
    }
}
