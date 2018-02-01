package com.wzhd.zk.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ApplicationClient {
	
	private ZooKeeper zk = null;
	private Stat stat = new Stat();
	private volatile List<String> serverList = null;
	
	private void connectZK() throws IOException, KeeperException, InterruptedException {
		zk = new ZooKeeper(BaseData.HOSTS, BaseData.SESSION_TIMEOUT, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getType() == Event.EventType.NodeDeleted && 
						BaseData.PARENT_PATH.equals(event.getPath())) {
					try {
						updateServerList();
					} catch (KeeperException e) {
					    e.printStackTrace();
					} catch (InterruptedException e) {
					    e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
					    e.printStackTrace();
					}
				}
			}
		});
		updateServerList();
	}
	
	private void updateServerList() throws KeeperException, InterruptedException, UnsupportedEncodingException {
		List<String> newServerList = new ArrayList<String>();

        //获取并监听groupNode子节点变化
        //watch参数为true，表示监听子节点变化事件
        //每次都需要重新注册监听，因为一次注册，只能监听一次事件，如果还想继续保持监听，必须重新注册
        List<String> subList = zk.getChildren(BaseData.PARENT_PATH,true);
        if (subList.size() < 3) {
        	try {
				Process child = Runtime.getRuntime().exec("D:\\test\\run.bat");
				InputStream in = child.getInputStream();
				int c;
				while ((c = in.read()) != -1) {
					System.out.print((char)c);
				}
				
				in.close();
				try {
					child.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("done");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        for (String subNode : subList){
            byte[] data = zk.getData(BaseData.PARENT_PATH+"/"+subNode,false,stat);
            newServerList.add(new String(data,"utf-8"));
        }

        //替换server列表
        serverList = newServerList;
        System.out.println("server list updated:"+serverList);
	}
	
	/**
	 * 客户端的逻辑操作
	 * @throws InterruptedException
	 */
	private void handle() throws InterruptedException {
		Thread.sleep(Long.MAX_VALUE);
	}
	
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		ApplicationClient client = new ApplicationClient();
		client.connectZK();
		client.handle();
	}
}
