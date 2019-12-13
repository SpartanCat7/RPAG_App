package com.example.rpagv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ImageStorageTest {

    @Test
    public void imageStorage_GuardarBitmapComoJPG(){
        Bitmap bitmap = Bitmap.createBitmap(100,100,Bitmap.Config.RGB_565);

        String pic_name = "TestPic_" + new Date().getTime() + ".jpg";
        String pathname = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RPAG_Test";
        File file = MainActivity.GuardarBitmapComoJPG(bitmap,pathname,pic_name);

        assertTrue(file.exists());
    }



}