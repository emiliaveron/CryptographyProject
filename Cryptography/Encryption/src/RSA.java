import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSA {
    public final List<Integer> publicKey;
    public final List<Integer> privateKey;

    RSA(){
        List<Integer> primes = generatePrimes(10000);
        int p = primes.get(getRandomIndex(primes.size()));
        int q = primes.get(getRandomIndex(primes.size()));

        this.publicKey = generatePublicKey(p, q);
        int e = publicKey.get(0);
        this.privateKey = generatePrivateKey(p, q, e);
    }

    private int getRandomIndex(int max) {
        Random r = new Random();
        return r.nextInt(max);
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
        while (gcd(e, (p - 1) * (q - 1)) != 1) {
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
        while ((d * e) % ((p - 1) * (q - 1)) != 1) {
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

    public static int encrypt(int message, List<Integer> publicKey) {
        int e = publicKey.get(0);
        int n = publicKey.get(1);
        int encrypted = 1;
        while (e > 0) {
            encrypted *= message;
            encrypted %= n;
            e--;
        }
        return encrypted;
    }

    public static int decrypt(int encrypted, List<Integer> privateKey) {
        int decrypted = 1;
        int d = privateKey.get(0);
        int n = privateKey.get(1);
        while (d > 0) {
            decrypted *= encrypted;
            decrypted %= n;
            d--;
        }
        return decrypted;
    }
}
