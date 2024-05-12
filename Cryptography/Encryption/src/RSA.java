import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSA {
    public final List<Integer> publicKey;
    public final List<Integer> privateKey;
    public static final int limitNumPrimes = 100;

    RSA(){
        List<Integer> primes = generatePrimes(limitNumPrimes);
        primes.remove(0); primes.remove(0);

        int i = new Random().nextInt(primes.size());
        int p = primes.get(i);
        primes.remove(i);
        int q = primes.get(new Random().nextInt(primes.size()));

        List<List<Integer>> keys = generateKeys(p, q);

        this.publicKey = keys.get(0);
        this.privateKey = keys.get(1);
    }


    public static List<Integer> generatePrimes(int limit) {
        List<Integer> primes = new ArrayList<>();
        
        for (int i = 2; i <= limit; i++) {
            boolean isPrime = true;
            for (int j : primes) if (i % j == 0) isPrime = false;
            if (isPrime) primes.add(i);
        }
        return primes;
    }

    public static List<List<Integer>> generateKeys(int p, int q) {
        int n = p * q;
        int phi = (p-1) * (q-1);
        if (phi == 100 || phi <= 2){
            generatePrimes(limitNumPrimes);
        }

        System.out.println("Generating keys with p = " + p + " and q = " + q);
        System.out.println("n = " + n);
        System.out.println("phi = " + phi);

        int e = 2;
        while (e < phi){
            if (gcd(e,phi) == 1){
                System.out.println("Found e = " + e);
                break;
            }
            e += 1;
        }

        int d = 2;
        while (true){
            if ((d*e)%phi == 1){
                if (d*e > n){
                    System.out.println("Found d = " + d);
                    break;
                }
            }
            d += 1;
        }

        List<Integer> public_key = new ArrayList<>();
        public_key.add(e);
        public_key.add(n);

        List<Integer> private_key = new ArrayList<>();
        private_key.add(d);
        private_key.add(n);

        List<List<Integer>> keys = new ArrayList<>();
        keys.add(public_key);
        keys.add(private_key);

        System.out.println("Generated keys: ");
        System.out.println(keys.get(0).toString());
        System.out.println(keys.get(1).toString());

        return keys;
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
