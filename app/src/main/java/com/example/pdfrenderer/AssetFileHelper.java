package com.example.pdfrenderer;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetFileHelper {

    public static String copyAssetFileToCache(Context context, String assetFileName) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        FileOutputStream out;
        String cacheFilePath = context.getCacheDir().getPath() + "/" + assetFileName;

        try {
            in = assetManager.open(assetFileName);
            out = new FileOutputStream(cacheFilePath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.close();

            return cacheFilePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle the error as needed
        }
    }
}
