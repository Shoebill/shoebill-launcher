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
import java.util.Map;

public class ShoebillLauncher
{
	private static final String PROPERTY_JAR_FILES = "jarFiles";
	
	private static final String SHOEBILL_PATH = "shoebill/";
	private static final String BOOTSTRAP_FOLDER_NAME = "bootstrap/";
	
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
	
	
	public static Object resolveDependencies() throws Throwable
	{
		File folder = new File(SHOEBILL_PATH, BOOTSTRAP_FOLDER_NAME);
		ClassLoader classLoader = createUrlClassLoader(folder.listFiles(JAR_FILENAME_FILTER), null);
		Class<?> clz = classLoader.loadClass(DEPENDENCY_MANAGER_CLASS_NAME);
		Method method = clz.getMethod("resolveDependencies");
		
		try
		{
			return method.invoke(null);
		}
		catch (InvocationTargetException e)
		{
			throw e.getTargetException();
		}
	}

	@SuppressWarnings("unchecked")
	public static Object createShoebill(Object context) throws Throwable
	{
		Map<String, Object> properties = Map.class.cast(context);
		List<File> files = List.class.cast(properties.get(PROPERTY_JAR_FILES));
		
		File[] fileArray = new File[files.size()];
		files.toArray(fileArray);
		
		ClassLoader classLoader = createUrlClassLoader(fileArray, null);
		Class<?> clz = classLoader.loadClass(SHOEBILL_IMPL_CLASS_NAME);
		Constructor<?> constructor = clz.getConstructor();
		
		try
		{
			return constructor.newInstance();
		}
		catch (InvocationTargetException e)
		{
			throw e.getTargetException();
		}
	}
	
	private static URLClassLoader createUrlClassLoader(File[] files, ClassLoader parent)
	{
		List<URL> urls = new ArrayList<>();
		
		for(File file : files)
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
		
		URL[] urlArray = urls.toArray(new URL[urls.size()]);
		return URLClassLoader.newInstance(urlArray, parent);
	}
}
