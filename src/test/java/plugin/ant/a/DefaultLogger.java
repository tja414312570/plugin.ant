package plugin.ant.a;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.plugin.CustomProxy;
import com.yanan.framework.plugin.autowired.plugin.WiredStackContext;

@Register(register = Logger.class,signlTon = false)
public class DefaultLogger implements CustomProxy<Logger>{
	@Override
	public Logger getInstance() {
		Logger logger = LoggerFactory.getLogger(WiredStackContext.getRegisterDefintion().getRegisterClass());
		return logger;
	}
}
