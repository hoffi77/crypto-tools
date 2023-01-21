package de.alijas.cmd;

import de.alijas.dirproc.crypto.SecretFileHandler;

import java.nio.file.Paths;

public class SecretFileCli {

    String inputFilePath;

    String outputFilePath;

    SecretFileHandler sfh;

    public static void main(final String[] args)  {

        if (args.length < 4) {
            System.out.println("Usage: SecretFileCli <mode> <input-file-path> <output-file-path> <passphrase>");
            System.exit(1);
        }

        String mode = args[0];
        String inputFilePath = args[1];
        String outputFilePath = args[2];
        String passphrase = args[3];

        SecretFileCli cli = new SecretFileCli(inputFilePath, outputFilePath, passphrase, "LKDlskldklöakdö ködkaökasödk öakd");

        switch (mode) {
            case "e":
                cli.encryptFile();
                break;
            case "d":
                cli.decryptFile();
                break;
            default:
                System.err.println("Mode-Parameter must either be 'e' or 'd'.");
                System.exit(1);
        }
    }

    SecretFileCli(String inputFilePath, String outputFilePath, String password, String salt) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        sfh = new SecretFileHandler(password, salt, Paths.get(".").toFile().getAbsolutePath());
    }

    public void encryptFile() {
        sfh.encrypt(inputFilePath, outputFilePath);
    }

    public void decryptFile() {
        sfh.decrypt(inputFilePath, outputFilePath);
    }
}
