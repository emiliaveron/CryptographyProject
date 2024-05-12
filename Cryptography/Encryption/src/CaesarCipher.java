public class CaesarCipher {
    private final int shift;

    public CaesarCipher(int shift) {
        this.shift = shift;
    }

    public String encrypt(String plainText) {
        StringBuilder encryptedText = new StringBuilder();

        for (int i = 0; i < plainText.length(); i++) {
            char currentChar = plainText.charAt(i);
            if (Character.isLetter(currentChar)) {
                char encryptedChar = (char) (currentChar + shift);
                if ((Character.isLowerCase(currentChar) && encryptedChar > 'z')
                        || (Character.isUpperCase(currentChar) && encryptedChar > 'Z')) {
                    encryptedChar = (char) (currentChar - (26 - shift));
                }
                encryptedText.append(encryptedChar);
            } else {
                encryptedText.append(currentChar);
            }
        }
        return encryptedText.toString();
    }

    public String decrypt(String encryptedText) {
        StringBuilder decryptedText = new StringBuilder();

        for (int i = 0; i < encryptedText.length(); i++) {
            char currentChar = encryptedText.charAt(i);
            if (Character.isLetter(currentChar)) {
                char decryptedChar = (char) (currentChar - shift);
                if ((Character.isLowerCase(currentChar) && decryptedChar < 'a')
                        || (Character.isUpperCase(currentChar) && decryptedChar < 'A')) {
                    decryptedChar = (char) (currentChar + (26 - shift));
                }
                decryptedText.append(decryptedChar);
            } else {
                decryptedText.append(currentChar);
            }
        }
        return decryptedText.toString();
    }
}
