package cn.mikulink.rabbitbot.test.utils;

import cn.mikulink.rabbitbot.utils.QRCodeUtil;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * created by MikuNyanya on 2021/12/6 11:44
 * For the Reisen
 */
public class QRCodeUtilTest {

    @Test
    public void test() {
        try {
//            byte[] b = QRCodeUtil.createQRCode(200, 200, "VPYbo7I0KKto.4@");
//            OutputStream os = new FileOutputStream("E:\\qrtest.png");
//            os.write(b);
//            os.close();

            BufferedImage qrImg = QRCodeUtil.createQRCode("https://blog.csdn.net/cyan20115/article/details/106553811",
                    100, 100,
                    Color.pink, null);
            ImageIO.write(qrImg, "png", new File("E:\\qrImage.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
