package com.zzy.netty;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;



public class ClientFrame extends Frame {
	public static final ClientFrame INSTANCE = new ClientFrame();
	TextArea ta = new TextArea();
	TextField tf = new TextField();
	Client c = null;
	
	public ClientFrame() {
		this.setSize(600, 400);
		this.setLocation(100, 20);
		this.add(ta, BorderLayout.CENTER);
		this.add(tf, BorderLayout.SOUTH);
		tf.addActionListener(new ActionListener() {
			//按回车键出发该方法
			@Override
			public void actionPerformed(ActionEvent e) {
				String date = new Date().toLocaleString();
				String line = System.getProperty("line.separator");
				String context = tf.getText();
				c.send("张松岩：    "+date+line+context);
				//把字符串发送到服务器
				//ta.setText(ta.getText() + tf.getText());
				//清空消息区
				tf.setText("");
			}
		});
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				c.closeConnect();
				System.exit(0);
			}
			
		});
		
	}
	
	private void connectToServer() {
		c = new Client();
		c.connect();
	}
	
	public void updateText(String msgAccepted) {
		this.ta.setText(ta.getText() + System.getProperty("line.separator") + msgAccepted);
	}
	
	public static void main(String[] args) {
		ClientFrame cf = ClientFrame.INSTANCE; 
		cf.setVisible(true);
		cf.connectToServer();
	}
}
