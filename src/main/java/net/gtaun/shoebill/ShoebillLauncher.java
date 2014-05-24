package net.gtaun.shoebill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
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
	
	private static final FilenameFilter JAR_FILENAME_FILTER = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".jar");
		}
	};

	
	public static void loadNativeLibrary() throws ClassNotFoundException, SecurityException, IllegalArgumentException
	{
		ClassLoader.getSystemClassLoader().loadClass("net.gtaun.shoebill.SampNativeFunction");
		SampNativeFunction.loadLibrary();
	}
	
	public static Object resolveDependencies() throws Throwable
	{
		File folder = new File(SHOEBILL_PATH, BOOTSTRAP_FOLDER_NAME);
		
		try(URLClassLoader classLoader = createUrlClassLoader(folder.listFiles(JAR_FILENAME_FILTER), null))
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("dependencyManagerImpl.txt")));
			String implClass = reader.readLine();
			reader.close();
			
			Class<?> clz = classLoader.loadClass(implClass);
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
	}

	@SuppressWarnings("unchecked")
	public static Object createShoebill(Object context, int[] amxHandles) throws Throwable
	{
		Map<String, Object> properties = Map.class.cast(context);
		List<File> files = List.class.cast(properties.get(PROPERTY_JAR_FILES));
		
		URLClassLoader classLoader = createUrlClassLoader(files.toArray(new File[0]), ClassLoader.getSystemClassLoader());
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("shoebillImpl.txt")));
		String implClass = reader.readLine();
		reader.close();
		
		Class<?> clz = classLoader.loadClass(implClass);
		
		try
		{
			Constructor<?> constructor = clz.getConstructor(int[].class);
			return constructor.newInstance(amxHandles);
		}
		catch (NoSuchMethodException e)
		{
			System.out.println("Launcher Error: Can't find shoebill constructor, Maybe the shoebill library is outdated.");
			throw e;
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
