package com.douban.movie.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.douban.movie.ip.Address;
import com.douban.movie.ip.IpPool;

/**
 * <p>Title: FetchImgUtil</p>
 * <p>Description:下载图片到本地</p>
 * @author Administrator
 * @date 2018年2月3日 下午1:31:24
 */
public class FetchImgUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(FetchImgUtil.class); 
	//获取ip池对象
	private static IpPool ipPool = new IpPool();
	
	public static String fetchImg(String imgUrl) {
		
		//获取图片名字
		String imgName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
		String path = "E:\\java_soup\\image";
		//图片路径
		File dir = new File(path);
		//本地海报路径
		File file = new File(path + File.separator + imgName);
		//创建地址ip和端口
		Address address = null;
		try {
			if (!dir.exists()) {
				dir.createNewFile();
			}
			//获取地址对象
			address = ipPool.getIpAndPort();

			//获取图片地址
			URL url = new URL(imgUrl);
			//获取连接
			URLConnection con = url.openConnection(new Proxy(Proxy.Type.HTTP, 
					new InetSocketAddress(address.getIp(), address.getPort())));
			//设置连接时间
			con.setConnectTimeout(5000);
			//获取输入流
			InputStream is = con.getInputStream();
			//获取输出流
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			//创建缓冲流
			byte[] buf = new byte[1024];
			int size;
			while(-1 != (size = is.read(buf))) {
				bos.write(buf, 0, size);
			}
			bos.close();
			is.close();
		}catch(Exception e) {
			LOG.debug(e.getMessage());
			if (address != null) {
				ipPool.removeIpAndPort(address);
			}
		}
		//如果文件不存在则继续下载
		if (!file.exists()) {
			fetchImg(imgUrl);
		}
		
		return file.getPath();
	}
	
//	public static void main(String[] args) throws IOException {
//		String imgUrl = "https://img3.doubanio.com/view/photo/s_ratio_poster/public/p1955027201.jpg";
//		String location = fetchImg(imgUrl);
//		System.out.println(location);
//	}
	
}
