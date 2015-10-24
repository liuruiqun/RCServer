package l.rq.v1;

import java.io.*;

import javax.media.*;
import javax.media.Buffer;
import javax.media.format.*;
import javax.media.util.*;
import javax.media.control.*;
import java.awt.*;
import java.awt.image.*;
import com.sun.image.codec.jpeg.*;

public class CameraCapture implements Runnable
{
	public static Player player=null;
	private CaptureDeviceInfo di=null;
	private MediaLocator ml=null;
	private Buffer buf=null;
	private Image img=null;
	//private VideoFormat vf=null;
	private BufferToImage btoi=null;
	//private ImagePanel imgpanel=null;
	String str="vfw:Microsoft WDM Image Capture (Win32):0";
	
	private int rectX;
	private int rectY;
	private int rectWidth=300;
	private int rectHeight=200;
	private int imgWidth=320;
	private int imgHeight=240;
	
	Thread thread=null;
	
	public CameraCapture()
	{
		thread = new Thread(this);  //建立专门的线程
		thread.start();
		di = CaptureDeviceManager.getDevice(str);
		ml = di.getLocator();
		try
		{
			player = Manager.createRealizedPlayer(ml);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		player.start();
		sleep(4000);
		System.out.println("init OK");
	}
	public static void playerclose()
	{
		player.close();
		player.deallocate();
	}
	public void capture()
	{
		FrameGrabbingControl fgc = (FrameGrabbingControl)player.getControl("javax.media.control.FrameGrabbingControl");
		buf = fgc.grabFrame();// Convert it to an image
		btoi = new BufferToImage((VideoFormat)buf.getFormat());
		img = btoi.createImage(buf);// show the image
		sleep(2000);
		if(img == null)
		{
			System.out.println("capture Error");
			System.exit(1);
		}
		//imgpanel.setImage(img); // save image
		System.out.println("capture OK");
	}
	public void save()
	{
		if(img != null)
		{
			String path = "CameraCapture.jpg";
			saveJPG(path);
		}
	}
	public void saveJPG(String s)
	{

		System.out.println("save start");
		BufferedImage bi = new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_RGB);

		Graphics2D g2 = bi.createGraphics();
		g2.clipRect(rectX,rectY,rectWidth,rectHeight);
		g2.drawImage(img,null,null);
		int moveX = rectX>0 ? rectX:0;
		int moveY = rectY>0 ? rectY:0;
		int cutWidth = rectX+rectWidth>imgWidth ? rectWidth-((rectX+rectWidth)-imgWidth):rectWidth;
		int cutHeight = rectY+rectHeight>imgHeight ? rectHeight-((rectY+rectHeight)-imgHeight):rectHeight;
		bi = bi.getSubimage(moveX,moveY,cutWidth,cutHeight);
		File file=new File(s);
		if(!file.exists())
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		FileOutputStream out=null;
		try
		{
			out = new FileOutputStream(file);
		}
		catch(java.io.FileNotFoundException io)
		{
			System.out.println("File Not Found");
		}
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
		param.setQuality(1f,false);
		encoder.setJPEGEncodeParam(param);
		try
		{
			encoder.encode(bi);
			sleep(2000);
			out.close();
			System.out.println("save OK");
		}
		catch(java.io.IOException io)
		{
			System.out.println("IOException");
		}
	}
	
	private void sleep(int t)
	{
		try
		{
			Thread.sleep(t);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/*public static void main(String[] args) throws Exception
	{
		CameraCapture picture = new CameraCapture();
		picture.capture();
		picture.save();
		System.out.println("OK");
		System.exit(0);
	}
	public void run()
	{
		// TODO Auto-generated method stub
		sleep(500);
	}*/
}
