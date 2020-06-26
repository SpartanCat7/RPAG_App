package edu.integrator.rpagv2;

import android.graphics.Bitmap;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ImageStorageTest {

    @Test
    public void imageStorage_GuardarBitmapComoPNG(){
        Bitmap bitmap = Bitmap.createBitmap(100,100,Bitmap.Config.RGB_565);

        String pic_name = "TestPic_" + new Date().getTime() + ".png";
        String pathname = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RPAG_Test";
        File file = MainActivity.GuardarBitmapComoPNG(bitmap,pathname,pic_name);

        assertTrue(file.exists());
    }

    @Test
    public  void imageStorage_LeerFileComoByteArray(){
        Bitmap bitmap = Bitmap.createBitmap(100,100,Bitmap.Config.RGB_565);

        String pic_name = "TestPic_" + new Date().getTime() + ".png";
        String pathname = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RPAG_Test";
        File file = MainActivity.GuardarBitmapComoPNG(bitmap,pathname,pic_name);
        byte[] result = new byte[0];
        try {
            result = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(result.length > 0);
    }


}