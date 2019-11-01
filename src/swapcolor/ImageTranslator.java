package swapcolor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageTranslator {
    private BufferedImage originalImage;
    private BufferedImage workingImage;
    private Color colorToSwap;
    private Color colorToReplace;
    private double distMin;
    private int satSensi;
    private int brightSensi;
    private boolean b;
    private File[] files;
    

    public ImageTranslator(BufferedImage originalImage,File[] files) {
        this.originalImage = originalImage;
        this.files=files;
    }
    
    public BufferedImage makeChanges(){
        if(this.colorToReplace==null || this.colorToSwap==null)
            return this.workingImage;
        this.workingImage = this.deepCopy(this.originalImage);
        int width = this.workingImage.getWidth();
        int height = this.workingImage.getHeight();
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                
                int p = this.workingImage.getRGB(i, j);
                // >> rigth shift the least significant bit is lost and we place a 0 in front << its the contrary
                // & = AND and |=OR on bits
                int alpha = (p>>24) & 0xff;//in java to write hexa number we put 0x in front
                int red = (p>>16) & 0xff;
                int green = (p>>8) & 0xff;
                int blue = p & 0xff;
                if(alpha !=0){
                    float[] hsv = new float[3];
                    float[] hsv2 = new float[3];
                    float[] hsv3 = new float[3];
                    Color.RGBtoHSB(red,green,blue,hsv);//color value of the pixel of the image
                    Color.RGBtoHSB(this.colorToSwap.getRed(),this.colorToSwap.getGreen(),this.colorToSwap.getBlue(),hsv2);//color value of the color to change
                    Color.RGBtoHSB(this.colorToReplace.getRed(),this.colorToReplace.getGreen(),this.colorToReplace.getBlue(),hsv3);//color value of the color we will replace with
                    double hmin= Math.min(hsv[0], hsv2[0]);
                    double hmax= Math.max(hsv[0], hsv2[0]);
                    double dist = Math.min(Math.abs(hmax-hmin),Math.abs(hmax-1-hmin));//the hue value is like a circle so the dist is the min of the dist by going to the rigth and and the one by going to the left
                    double dist2 = (hsv2[1]-hsv[1]);
                    double dist3 = (hsv2[2]-hsv[2]);
                    if(this.b){//if we only change tint
                        if(dist<this.distMin){
                            p = Color.HSBtoRGB(hsv3[0], hsv[1], hsv[2]);
                            //printColor(p);
                            p = (alpha<<24) | (p & 0xffffff);
                            this.workingImage.setRGB(i, j, p);
                        }
                    }
                    else{
                        if(dist<this.distMin && (dist2*100)<this.satSensi && (dist3*100)<this.brightSensi){
                            hsv3[1] -= dist2;hsv3[1]=Math.max(Math.min(hsv3[1], 1), 0);
                            hsv3[2] -= dist3;hsv3[2]=Math.max(Math.min(hsv3[2], 1), 0);
                            p = Color.HSBtoRGB(hsv3[0], hsv3[1], hsv3[2]);
                            p = (alpha<<24) | (p & 0xffffff);
                            this.workingImage.setRGB(i, j, p);
                        }
                    }      
                }
            }
        }
        return this.workingImage;
    }

    
    public void setOriginalImage(BufferedImage originalImage) {
        this.originalImage = originalImage;
        this.workingImage = originalImage;
    }


    public void setColorToSwap(Color colorToSwap) {
        this.colorToSwap = colorToSwap;
    }

    public void setColorToReplace(Color colorToReplace) {
        this.colorToReplace = colorToReplace;
    }

    public BufferedImage getWorkingImage() {
        return this.workingImage;
    }

    public void setDistMin(int distMin) {
        this.distMin = distMin/100.0;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public void setSatSensi(int satSensi) {
        this.satSensi = satSensi;
    }

    public void setBrightSensi(int brightSensi) {
        this.brightSensi = brightSensi;
    }
    
    private BufferedImage deepCopy(BufferedImage bi) {//make a copy of the image to work with
    ColorModel cm = bi.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
}
    
    public boolean saveAll(File f){
        if(!f.isDirectory())
            return false;
        String path = f.getAbsolutePath();
        for (int i=0;i<this.files.length;i++) {
            String[] tab = this.files[i].getName().split("\\.");//get the name and the format of the image
            String pathFile = path + File.separator + this.files[i].getName();
            try {
                BufferedImage img = ImageIO.read(this.files[i]);
                File outputfile = new File(pathFile);
                int k = 1;
                while(outputfile.exists()){//we change the name so it doesnt overwrite an existing file
                    pathFile = path + File.separator + tab[0] + "(" + k + ")." + tab[1];
                    outputfile = new File(pathFile);
                    k++;
                }
                this.workingImage = this.deepCopy(img);// we make the current image as the working one
                img = this.makeChanges();//we apply changes
                ImageIO.write(img, tab[1], outputfile);//we save it
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private void printColor(int p) {//Test Function to print the rgba value of p
        int alpha = (p>>24) & 0xff;
        int red = (p>>16) & 0xff;
        int green = (p>>8) & 0xff;
        int blue = p & 0xff;
        System.out.println(alpha+" "+red+" "+green+" "+blue);
    }

}
