package net.lxsthw.redehades.blacklist.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilizada para mexer com files.
 *
 * @author TalonDev
 */
public class FileUtils {

    private static final Logger LOGGER = new ModuleLogger("HadesBlacklist").getModule("FileUtils");

    /**
     * Deleta um arquivo, se for uma pasta ir� deletar e ele e tudo que tem dentro.
     *
     * @param file O arquivo para deletar.
     */
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File delete : file.listFiles()) {
                deleteFile(delete);
            }
        }

        file.delete();
    }

    /**
     * Copia um arquivo para outro destinario.
     *
     * @param in O arquivo que ir� ser copiado.
     * @param out O destino para copiar.
     */
    public static void copyFiles(File in, File out) {
        if (in.isDirectory()) {
            if (!out.exists()) {
                out.mkdir();
            }

            for (File file : in.listFiles()) {
                copyFiles(file, new File(out, file.getName()));
            }
        } else {
            InputStream input = null;
            FileOutputStream ou = null;
            try {
                input = new FileInputStream(in);
                ou = new FileOutputStream(out);
                byte[] buff = new byte[1024];
                int len;
                while ((len = input.read(buff)) > 0) {
                    ou.write(buff, 0, len);
                }
                ou.close();
                input.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed at copy file " + out.getName() + "!", ex);
            } finally {
                try {
                    if (ou != null) {
                        ou.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Copia um arquivo apartir de um InputStream.
     *
     * @param input O input para ser copiado.
     * @param out O arquivo destinario.
     */
    public static void copyFile(InputStream input, File out) {
        FileOutputStream ou = null;
        try {
            ou = new FileOutputStream(out);
            byte[] buff = new byte[1024];
            int len;
            while ((len = input.read(buff)) > 0) {
                ou.write(buff, 0, len);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed at copy file " + out.getName() + "!", ex);
        } finally {
            try {
                if (ou != null) {
                    ou.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
