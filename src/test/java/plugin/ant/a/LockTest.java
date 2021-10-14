package plugin.ant.a;

import com.yanan.framework.ant.channel.socket.LockSupports;

public class LockTest {
	public static void main(String[] args) {
		Object obj = new Object();
		new Thread(()->{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LockSupports.set(obj, obj, "test t");
			LockSupports.unLock(obj);
		}).start();
		LockSupports.lock(obj);
		System.err.println("hello world");
		System.err.println("get:"+LockSupports.get(obj, obj));
	}
}
