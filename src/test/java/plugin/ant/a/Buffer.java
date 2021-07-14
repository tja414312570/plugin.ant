package plugin.ant.a;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import com.yanan.utils.CacheHashMap;
import com.yanan.utils.reflect.TypeToken;

public class Buffer {
	private String context;
	public Buffer(String context) {
		this.context=context;
	}
	@Override
	public String toString() {
		return "Buffer [context=" + context + "]";
	}
	public static void main(String[] args) {
		CacheHashMap<Integer,Buffer> lockMap = new CacheHashMap<>(new TypeToken<WeakReference<?>>() {}.getTypeClass());
		
		Buffer buffer ;
		AtomicInteger ai = new AtomicInteger();
		for(int i = 0;i<100000000;i++) {
//			if(i % 1000 == 0)
//				System.gc();
			Integer count = ai.getAndIncrement();
			buffer = new Buffer(i+"test");
			lockMap.puts(count, buffer);
			if(lockMap.get(count) == null) {
				throw new RuntimeException(count+"");
			}
			Object obj = buffer.context;
			obj = count;
//			System.err.println(i+"==>"+lockMap.get(count)+"==>"+lockMap.size());
		}
	}
}
