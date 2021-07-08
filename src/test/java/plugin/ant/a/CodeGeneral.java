package plugin.ant.a;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.yanan.utils.resource.scanner.Path;
import com.yanan.utils.resource.scanner.Path.PathInter;


public class CodeGeneral {
	public static int sumFile = 0;// 总文件数  
    public static int sumLine = 0;// 有效行数 
    public static int allLine = 0;//总行数
	public static void main(String[] args) {
		 try {  
			 Path path = new Path("/Volumes/GENERAL/git/plugin.ant.core/src/main/java/com");///Users/yanan/Workspaces/MyEclipse 2017 CI/springFramework
			 path.filter("**/a/**.java");
			 path.scanner(new PathInter() {
				@Override
				public void find(File file) {
					 try {
						 BufferedReader br = new BufferedReader(new InputStreamReader( new FileInputStream(file), "utf-8"));//以utf-8 格式读入，若文件编码为gkb 则改为gbk  
						    String s = null;  
						    while ((s = br.readLine()) != null) {  
						        String line = s.replaceAll("\\s", "");// \\s表示 空格,回车,换行等空白符,  
						        if (!"".equals(line)  
						                &&! line.startsWith("//")  
						                &&! line.startsWith("/*")    
						                &&! line.startsWith("/**")   
						                &&! line.startsWith("*")) {  
						        	 sumLine ++;
						            System.out.println(sumLine + "：" + s);  
						        }
						        allLine ++;
						    }  
						sumFile++;  
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
	           System.out.println("一共：" + sumFile + "个类\t\t" + sumLine + "行有效代码！");  
	           System.out.println("总行数："+allLine);
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	    }  
	
}
