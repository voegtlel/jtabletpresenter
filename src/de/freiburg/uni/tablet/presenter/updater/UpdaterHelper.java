package de.freiburg.uni.tablet.presenter.updater;

import java.io.File;
import java.io.IOException;

public class UpdaterHelper {
	
	
	/**
	 * Deleted files and folder recursively
	 * @param f
	 * @return
	 */
	public static boolean deleteFile(final File f) {
		if (f.isDirectory()) {
			for (File subF : f.listFiles()) {
				deleteFile(subF);
			}
		}
		
		return f.delete();
	}
	
	/**
	 * Creates a new jvm
	 * @param mainClass
	 * @param classPath the classPath or null to use the same
	 * @return
	 * @throws IOException
	 */
	public static Process runJvm(final String mainClass, String classPath, final String... arguments) throws IOException {
		if (classPath == null) {
			classPath = System.getProperty("java.class.path");
		}
		String javaPath =  System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String[] args = new String[4 + arguments.length];
		args[0] = javaPath;
		args[1] = "-cp";
		args[2] = classPath;
		args[3] = mainClass;
		for (int i = 0; i < arguments.length; i++) {
			args[i+4] = arguments[i];
		}
		ProcessBuilder pb = new ProcessBuilder(args);
		return pb.start();
	}
}
