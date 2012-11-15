package net.gtaun.shoebill.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ShoebillLauncher
{
	private static final String SHOEBILL_PATH = "./shoebill/";
	
	private static final String BOOTSTRAP_FOLDER_NAME = "bootstrap";
	private static final String LIBRARIES_FOLDER_NAME = "libraries";
	
	private static final String DEPENDENCY_MANAGER_CLASS_NAME = "net.gtaun.shoebill.dependency.ShoebillDependencyManager";
	private static final String SHOEBILL_IMPL_CLASS_NAME = "net.gtaun.shoebill.ShoebillImpl";
	
	private static final FilenameFilter JAR_FILENAME_FILTER = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".jar");
		}
	};
	
	
	public static boolean resolveDependencies() throws Throwable
	{
		File folder = new File(SHOEBILL_PATH + BOOTSTRAP_FOLDER_NAME);
		ClassLoader classLoader = createUrlClassLoader(new File[]{folder}, null);
		Class<?> clz = classLoader.loadClass(DEPENDENCY_MANAGER_CLASS_NAME);
		Method method = clz.getMethod("main", String[].class);
		
		try
		{
			method.invoke(null, (Object)null);
		}
		catch (InvocationTargetException e)
		{
			throw e.getTargetException();
		}
		
		return true;
	}
	
	public static Object createShoebill() throws Throwable
	{
		File folder = new File(SHOEBILL_PATH + LIBRARIES_FOLDER_NAME);
		ClassLoader classLoader = createUrlClassLoader(new File[]{folder}, null);
		Class<?> clz = classLoader.loadClass(SHOEBILL_IMPL_CLASS_NAME);
		Constructor<?> constructor = clz.getConstructor();
		return constructor.newInstance();
	}
	
	private static URLClassLoader createUrlClassLoader(File[] folders, ClassLoader parent)
	{
		List<URL> urls = new ArrayList<>();
		
		for(File folder : folders)
		{
			File[] files = folder.listFiles(JAR_FILENAME_FILTER);
			if(files == null) continue;
			
			for (File file : files)
			{
				try
				{
					URL url = file.toURI().toURL();
					urls.add(url);
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}	
		}
		
		URL[] urlArray = urls.toArray(new URL[urls.size()]);
		return URLClassLoader.newInstance(urlArray, parent);
	}
}
