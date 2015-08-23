import java.awt.AWTException;
//import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.imageio.ImageIO;


public class RCServer extends JFrame{
	// 以指定字节数组创建准备接收数据的DatagramPacket对象  
    //private DatagramPacket inPacket = null;   
    // 定义一个用于发送的DatagramPacket对象  
    private DatagramPacket outPacket = null;
	private static int port;
	private static double mx;	//电脑鼠标的横坐标
	private static double my;	//电脑鼠标的纵坐标
	ServerThread serverthread; //初始化线程
	final JTextField messagebox;
	final JTextField field;
	final JButton stopbutton;
	final JButton startbutton;
	static int menux  =0; //menux信号量 0表示未开启 1表示开启 2表示暂停
	String message =null;
	String[] messages =null;
	String type =null;
	String info =null;
	public RCServer(){
		super();
        setTitle("遥控小精灵");
        setSize(230, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Toolkit toolkit = getToolkit(); // 获得Toolkit对象
        Dimension dimension = toolkit.getScreenSize(); // 获得Dimension对象
        int screenHeight = dimension.height; // 获得屏幕的高度
        int screenWidth = dimension.width; // 获得屏幕的宽度
        int frm_Height = this.getHeight(); // 获得窗体的高度
        int frm_width = this.getWidth(); // 获得窗体的宽度
        setLocation((screenWidth - frm_width) / 2,
                (screenHeight - frm_Height) / 2); // 使用窗体居中显示
        
        getContentPane().setLayout(null);
        final JLabel label = new JLabel();
        try {
			label.setText("本机IP："+InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        label.setBounds(10, 20, 300, 25);
        Font font = new Font("SimSun", Font.PLAIN, 16);
        label.setFont(font);
        getContentPane().add(label);
        
        final JLabel label2 =new JLabel();
    	label2.setText("请输入端口号：");
    	label2.setBounds(10, 50, 100, 25);
    	getContentPane().add(label2);
    	
    	field = new JTextField();
    	field.setBounds(110,50,90,25);
    	getContentPane().add(field);
        
    	startbutton = new JButton();
    	startbutton.setText("开启");
    	startbutton.setBounds(10,90, 80, 25);
    	getContentPane().add(startbutton);
    	
    	stopbutton = new JButton();
    	stopbutton.setText("停止");
    	stopbutton.setEnabled(false);
    	stopbutton.setBounds(120,90, 80, 25);
    	getContentPane().add(stopbutton);
    	
        final JLabel label3 =new JLabel();
    	label3.setText("请在手机端输入 本机IP 和 端口号");
    	label3.setBounds(10, 120, 280, 25);
    	getContentPane().add(label3);
    	
    	final JLabel label4 =new JLabel();
    	label4.setText("接收到信息：");
    	label4.setBounds(10, 150, 280, 20);
    	getContentPane().add(label4);
    	
    	messagebox = new JTextField();
    	messagebox.setBounds(10,180,190,25);
    	messagebox.enable(false);
    	getContentPane().add(messagebox);
    	
    	final JLabel label5 =new JLabel();
    	label5.setText("左手码农");
    	label5.setBounds(10, 220, 190, 25);
    	getContentPane().add(label5);
        
    	
    	
    	startbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	String str  = field.getText().trim();
            	int num;
            	if(str.equals("")){
            		JOptionPane.showMessageDialog(null,"请输入端口号");
            		return;
            	}
            	try{
            		num = Integer.parseInt(str);
            	}catch(Exception e){
            		JOptionPane.showMessageDialog(null,"端口号应该为数字");
            		return;
            	}
            	if(num<0||num>65535){
            		JOptionPane.showMessageDialog(null,"端口号应该大于0小于65535");
            		return;
            	}
            	port=num;
            	stopbutton.setEnabled(true);
            	startbutton.setEnabled(false);
            	start();
            	
            }
        });
    	
		
    	stopbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
 //           	startbutton.setEnabled(false);
            	stop();
            	stopbutton.setEnabled(false);
            	startbutton.setEnabled(true);
            }
    	 });
		
    	
    	setVisible(true);
	}
	
	 
	public static void main(String[] args) {
		
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

		java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
            	new RCServer();
            }
        });
	
	}
		
	public void start(){
		if(menux==0){   //menux信号量 0表示未开启 1表示开启 2表示暂停
			serverthread  =new ServerThread();
			serverthread.start();
			menux=1;
			messagebox.setText("开启信息监听");
			field.setEditable(false);
		}
		if(menux==2){
			serverthread.resume();
			menux=1;
			messagebox.setText("恢复信息监听");
		}
	}
	
	public void stop(){
		if(menux==1){
			serverthread.suspend();
			menux=2;
			messagebox.setText("暂停信息监听");
		}
		
	}
	
	
	public class ServerThread extends Thread{
    			
    	public void run(){
    		try {
    			//创建一个DatagramSocket对象，并指定监听的端口号
    			DatagramSocket socket;
    			try{
    				socket = new DatagramSocket(port);
    			}catch(Exception e){
    				messagebox.setText("端口已被使用,请更换端口");
    				startbutton.setEnabled(true);
    				stopbutton.setEnabled(false);
    				menux=0;
    				
    				
    				field.setEditable(true);
    				return;
    			}
				byte data [] = new byte[1024];
				//创建一个空的DatagramPacket对象
				DatagramPacket inPacket = new DatagramPacket(data,data.length);
				//使用receive方法接收客户端所发送的数据
				System.out.println(
						"开启端口监听"+socket.getLocalPort()
						);
				while(true){
					socket.receive(inPacket);
					System.out.print(inPacket.getSocketAddress());
					//socket.send(packet);
					/*System.out.println("send");
					System.out.println(
							"本地IP"+socket.getLocalAddress()
							+"开启端口监听"+socket.getLocalPort()
							+"远程IP"+packet.getAddress()
							+"远程端口"+packet.getSocketAddress());*/
					
					//System.out.println(new String(packet.getData()));    
					
					message = new String(inPacket.getData(),inPacket.getOffset(),inPacket.getLength());
					//System.out.println("message--->" + message);
					messagebox.setText(message);
					messages = message.split(":");
										
					if(messages.length>=2){
						type= messages[0];
						info= messages[1];
						if(type.equals("mouse"))
							MouseMove(info);
						if(type.equals("leftButton"))
							LeftButton(info);
						if(type.equals("rightButton"))
							RightButton(info);
						if(type.equals("mousewheel"))
							MouseWheel(info);
						if(type.equals("keyboard"))
							KeyBoard(info,socket);
						if(type.equals("screen"))
							Screen(info,socket,inPacket);
					}
				
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	public void MouseMove(String info){
    		String args[]=info.split(",");
    		String x= args[0];
    		String y= args[1];
			float px = Float.valueOf(x);
			float py = Float.valueOf(y);
			
			PointerInfo pinfo = MouseInfo.getPointerInfo();		//得到鼠标的坐标
			java.awt.Point p = pinfo.getLocation();
			mx=p.getX();	//得到当前电脑鼠标的坐标
			my=p.getY();
			java.awt.Robot robot;
			try {
				robot = new Robot();
//				System.out.println(mx+","+my);
//				System.out.println(px+","+py);
				robot.mouseMove((int)mx+(int)px,(int)my+(int)py);
			} catch (AWTException e) {
				e.printStackTrace();
			}
			
    	}
    	
    	public void LeftButton(String info) throws AWTException{
    		java.awt.Robot robot = new Robot();
    		if(info.equals("down"))
				robot.mousePress(InputEvent.BUTTON1_MASK);				
    		else if(info.equals("release"))
    			robot.mouseRelease(InputEvent.BUTTON1_MASK);
    		else if(info.equals("up"))
    			robot.mouseRelease(InputEvent.BUTTON1_MASK);
    		else if(info.equals("click")){
    			robot.mousePress(InputEvent.BUTTON1_MASK);
    			robot.mouseRelease(InputEvent.BUTTON1_MASK);
    		}
    	}
    	
    	public void RightButton(String info) throws AWTException{
    		java.awt.Robot robot = new Robot();
    		if(info.equals("down"))
				robot.mousePress(InputEvent.BUTTON3_MASK);				
    		else if(info.equals("release"))
    			robot.mouseRelease(InputEvent.BUTTON3_MASK);
    		else if(info.equals("up"))
    			robot.mouseRelease(InputEvent.BUTTON3_MASK);
    	}
    	
    	public void MouseWheel(String info)throws AWTException{
    		java.awt.Robot robot = new Robot();
    		float num = Float.valueOf(info);
    		if(num>0)
    			robot.mouseWheel(1);
    		else
    			robot.mouseWheel(-1);
    	}
    	
    	public void KeyBoard(String info,DatagramSocket socket)throws AWTException, IOException{
    		String args[]=info.split(",");
    		String type=null;
    		String cont=null;
    		String keystate =null;
    		java.awt.Robot robot = new Robot();
    		if(args.length==2){
    			type = args[0];
    			cont = args[1];
    		}
    		if(args.length==3){
    			type = args[0];
    			cont = args[1];
    			keystate = args[2];
    		}
    		
    		
    		if(type.equals("message")){
    			//Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
    			//cb.setContents(new StringSelection(cont), null);//调用粘贴板
    
    			
    			//robot.keyPress(KeyEvent.VK_CONTROL);
    			//robot.keyPress(KeyEvent.VK_V);
    			//robot.keyRelease(KeyEvent.VK_CONTROL);
    			//robot.keyRelease(KeyEvent.VK_V);  
    	        //发送
    	        //socket.send(p);
    		}else if(type.equals("key")){
    			if(cont.equals("BackSpace")){
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_BACK_SPACE);
    					robot.keyRelease(KeyEvent.VK_BACK_SPACE);
    				}
    			}
    			if(cont.equals("Enter")){
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_ENTER);
    					robot.keyRelease(KeyEvent.VK_ENTER);
    				}
    			}
    			if(cont.equals("Up")){
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_UP);
    					robot.keyRelease(KeyEvent.VK_UP);
    				}
    					
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_UP);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_UP);
    			}
    			if(cont.equals("Down")){
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_DOWN);
    					robot.keyRelease(KeyEvent.VK_DOWN);
    				}
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_DOWN);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_DOWN);
    			}
    			if(cont.equals("Left")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_LEFT);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_LEFT);
    			}
    			if(cont.equals("Right")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_RIGHT);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_RIGHT);
    			}
    			if(cont.equals("W")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_W);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_W);
    			}
    			if(cont.equals("S")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_S);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_S);
    			}
    			if(cont.equals("A")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_A);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_A);
    			}
    			if(cont.equals("S")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_S);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_S);
    			}
    			
    			if(cont.equals("Ctrl")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_CONTROL);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_CONTROL);
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_CONTROL);
    					robot.keyRelease(KeyEvent.VK_CONTROL);
    				}
    			}
    			
    			if(cont.equals("Z")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_Z);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_Z);
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_Z);
    					robot.keyRelease(KeyEvent.VK_Z);
    				}
    			}
    			
    			if(cont.equals("Space")){
    				if(keystate.equals("down"))
    					robot.keyPress(KeyEvent.VK_SPACE);
    				if(keystate.equals("up"))
    					robot.keyRelease(KeyEvent.VK_SPACE);
    				if(keystate.equals("click")){
    					robot.keyPress(KeyEvent.VK_SPACE);
    					robot.keyRelease(KeyEvent.VK_SPACE);
    				}
    			}
    			
    			
    		}else if(type.equals("dosmessage")){
   							
			    try {  
			    	String message = cont;
			    	String[] cmd = {message};
			    	Runtime.getRuntime().exec(cmd);    
	                System.out.println(" success!");  	                	    	        	    	        	                
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }   
    		}
    	}
    	
    	public void Screen(String info,DatagramSocket socket,DatagramPacket inPacket) throws AWTException, IOException{
    		String args[]=info.split(",");
    		String type=null;
    		String cont=null;
    		String keystate =null;
    		
    		if(args.length==2){
    			type = args[0];
    			cont = args[1];
    		}
    		if(args.length==3){
    			type = args[0];
    			cont = args[1];
    			keystate = args[2];
    		}
    		
    		
    		if(type.equals("message")){
    			if(cont.equals("Start")){
	                Robot picture = new Robot();
	    	        //设置Robot产生一个动作后的休眠时间,否则执行过快
	    			picture.setAutoDelay(1000);
	    	        //获取屏幕分辨率
	    	        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	    	        System.out.println(d);
	    	        Rectangle screenRect = new Rectangle(d);
	    	        //截图
	    	        BufferedImage bufferedImage =  picture.createScreenCapture(screenRect);
	    	        //保存截图
	    	        File file = new File("screenRect.png");
	    	        ImageIO.write(bufferedImage, "png", file);
	    	        //压缩
	    	        System.out.println("开始：" + new Date().toLocaleString());  
	    	        CompressPic imgCom = new CompressPic("D:\\Workspaces\\MyEclipse Professional 2014\\RCServer\\screenRect.png");  
	    	        imgCom.resizeFix(400, 400);  
	    	        System.out.println("结束：" + new Date().toLocaleString());
	    	        //发送
	    	        FileInputStream fis = new FileInputStream("D:/Workspaces/MyEclipse Professional 2014/RCServer/screenRect.png");    
	    	        BufferedInputStream bos = new BufferedInputStream(fis);
	    	        
	    	        byte [] sendData = new byte[1024];
	    	        byte [] ack = new byte[10];
	    	        //用于保存实际读取的字节数
	    	        int hasRead;
	    	        
	    	        //System.out.println("Server starting ...\n");
	    	        //DatagramSocket s = new DatagramSocket(10000);
	    	        //DatagramSocket ssocket;
	    	        //inPacket = new DatagramPacket(ack, ack.length);
	    	        
	    	        
	    	        
	    	        //使用循环来重复读取数据  
	    	        while((hasRead = bos.read(sendData)) != -1)
	    	        {	    	        		    	       	
	    	        	//System.out.println("getLength" + inPacket.getLength());
	    	        	//System.out.println("getLength" + inPacket.getOffset());
	    	        	System.out.println("send ...\n");
	    	             
	    	        	//将字节数组转换为字符串输出  
	    	            System.out.print(new String(sendData,0,hasRead));
	    	            System.out.print("-----------------------------");
	    	            //System.out.print(inPacket.getAddress());
	    	            //System.out.print(inPacket.getPort());
	    	            
	    	            outPacket = new DatagramPacket(sendData   
	    	                    , hasRead 
	    	                    , inPacket.getAddress()
	    	                    , inPacket.getPort());
	    	        	
	    	        	socket.send(outPacket);
	    	        	
	    	        	//System.out.println("recv ...\n");
	    	        	//socket.receive(inPacket);
	    	        	//System.out.println(new String (inPacket.getData()));
	    	        }
	    	        
	    	        fis.close();
	    	        bos.close();
    			}
    		}
    	}
	}

}
