package com.YaNan.frame.ant.utils;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.RegisterResult;
import com.esotericsoftware.kryo.Kryo;

public class SerialUtils {
	private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>();
	public static Kryo getKryo() {
		Kryo kryo = kryoLocal.get();
		if(kryo==null) {
			kryo = newKyro();
	        kryoLocal.set(kryo);
		}
		return kryoLocal.get();
	}
	private static Kryo newKyro() {
		Kryo kryo = new Kryo();
        kryo = new Kryo();
		kryo.setReferences(false);
		kryo.setRegistrationRequired(false);
		kryo.register(AntMessagePrototype.class);
		kryo.register(RegisterResult.class);
		kryo.register(Object[].class);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                    new StdInstantiatorStrategy()));
        return kryo;
	}
}
