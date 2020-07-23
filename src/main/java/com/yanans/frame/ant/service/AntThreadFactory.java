package com.yanan.frame.ant.service;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.yanan.frame.ant.AntContext;

public class AntThreadFactory implements ThreadFactory{
      private final ThreadGroup group;
      private final AtomicInteger threadNumber = new AtomicInteger(1);
      private final String namePrefix;
      protected AntContext antContext;
      

      AntThreadFactory(AntContext context) {
          SecurityManager s = System.getSecurityManager();
          antContext = context;
          group = (s != null) ? s.getThreadGroup() :
                                Thread.currentThread().getThreadGroup();
          namePrefix = "Ant-process-pool-thread-"+
        		  				context.getContextConfigure().getName()+
                                "-"+
                                context.getContextConfigure().getMaxProcess();
      }

      public Thread newThread(Runnable r) {
          Thread t = new Thread(group, r,
                                namePrefix + threadNumber.getAndIncrement(),
                                0);
          if (t.isDaemon())
              t.setDaemon(false);
          if (t.getPriority() != Thread.NORM_PRIORITY)
              t.setPriority(Thread.NORM_PRIORITY);
          return t;
      }

}