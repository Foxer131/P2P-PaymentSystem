package com.p2ppayment.security;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Classe de utilitários para operações criptográficas RSA.
 * Utiliza BigInteger para garantir a segurança, permitindo chaves de tamanho arbitrário.
 * Responsável por gerar, salvar, carregar, criptografar e descriptografar.
 */
public class RSA {

    // Comprimento da chave em bits. 2048 é um padrão seguro moderno.
    private static final int BIT_LENGTH = 2048;
    // Expoente público padrão. 65537 é um primo de Fermat e é computacionalmente eficiente.
    private static final BigInteger PUBLIC_EXPONENT = new BigInteger("65537");
    // Gerador de números aleatórios criptograficamente seguro.
    private static final SecureRandom random = new SecureRandom();

    // -- Registos (Records) para representar as chaves de forma estruturada --
    // Um record é uma forma concisa de criar uma classe de dados imutável.
    public record PublicKey(BigInteger e, BigInteger n) {}
    public record PrivateKey(BigInteger d, BigInteger n) {}
    public record KeyPair(PublicKey publicKey, PrivateKey privateKey) {}

    /**
     * Gera um novo par de chaves RSA (pública e privada).
     * @return Um objeto KeyPair contendo as novas chaves.
     */
    public static KeyPair generateKeyPair() {
        BigInteger p = BigInteger.probablePrime(BIT_LENGTH / 2, random);
        BigInteger q = BigInteger.probablePrime(BIT_LENGTH / 2, random);

        BigInteger n = p.multiply(q);

        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        BigInteger d = PUBLIC_EXPONENT.modInverse(phi);

        PublicKey publicKey = new PublicKey(PUBLIC_EXPONENT, n);
        PrivateKey privateKey = new PrivateKey(d, n);
        
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Criptografa uma mensagem de texto usando uma chave pública.
     * @param plainText A mensagem a ser criptografada.
     * @param key A chave pública a ser usada.
     * @return A mensagem criptografada, codificada em Base64 para fácil transmissão.
     */
    public static String encrypt(String plainText, PublicKey key) {
        BigInteger message = new BigInteger(plainText.getBytes(StandardCharsets.UTF_8));
        BigInteger encrypted = message.modPow(key.e(), key.n());
        return Base64.getEncoder().encodeToString(encrypted.toByteArray());
    }

    /**
     * Descriptografa uma mensagem usando uma chave privada.
     * @param cipherText A mensagem criptografada (em Base64).
     * @param key A chave privada a ser usada.
     * @return A mensagem original em texto plano.
     */
    public static String decrypt(String cipherText, PrivateKey key) {
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        BigInteger encrypted = new BigInteger(decodedBytes);
        BigInteger decrypted = encrypted.modPow(key.d(), key.n());
        return new String(decrypted.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Salva um par de chaves em ficheiros.
     * @param keyPair O par de chaves a ser salvo.
     * @param username O nome de utilizador, usado para nomear os ficheiros.
     * @throws java.io.IOException Se ocorrer um erro ao escrever nos ficheiros.
     */
    public static void saveKeyPair(KeyPair keyPair, String path) throws java.io.IOException {
        // Formato: e:n ou d:n
        String publicKeyContent = keyPair.publicKey().e() + ":" + keyPair.publicKey().n();
        String privateKeyContent = keyPair.privateKey().d() + ":" + keyPair.privateKey().n();

        Files.writeString(Path.of(path + ".pub"), publicKeyContent);
        Files.writeString(Path.of(path + ".key"), privateKeyContent);
    }

    /**
     * Carrega uma chave pública de um ficheiro .pub.
     * @param username O nome do utilizador cuja chave deve ser carregada.
     * @return Um objeto PublicKey.
     * @throws java.io.IOException Se ocorrer um erro ao ler o ficheiro.
     */
    public static RSA.PublicKey loadPublicKey(String path) throws java.io.IOException {
        String content = Files.readString(Path.of(path + ".pub"));
        String[] parts = content.split(":");
        BigInteger e = new BigInteger(parts[0]);
        BigInteger n = new BigInteger(parts[1]);
        return new PublicKey(e, n);
    }

    /**
     * Carrega uma chave privada de um ficheiro .key.
     * @param username O nome do utilizador cuja chave deve ser carregada.
     * @return Um objeto PrivateKey.
     * @throws java.io.IOException Se ocorrer um erro ao ler o ficheiro.
     */
    public static PrivateKey loadPrivateKey(String keyPath) throws java.io.IOException {
        Path path;
        if (keyPath.endsWith(".key")) {
            path = Path.of(keyPath);
        } else {
            path = Path.of(keyPath + ".key");
        }
        String content = Files.readString(path);
        String[] parts = content.split(":");
        return new PrivateKey(new BigInteger(parts[0]), new BigInteger(parts[1]));
    }
}
