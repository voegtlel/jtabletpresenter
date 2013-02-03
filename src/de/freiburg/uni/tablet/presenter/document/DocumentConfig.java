package de.freiburg.uni.tablet.presenter.document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DocumentConfig {
	public static class KeyValue {
		public final String key;
		public Object value;
		
		public KeyValue(String key, Object value) {
			this.key = key;
			this.value = value;
		}
	}
	
	private List<KeyValue> _data = new ArrayList<KeyValue>();
	private boolean _configChanged = false;
	
	public DocumentConfig(String filename) {
		try {
			FileInputStream fis = new FileInputStream(new File(filename));
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);
			try {
				String line = reader.readLine();
				int iLine = 0;
				while (line != null) {
					line = line.trim();
					iLine++;
					if (line.startsWith("#") || line.startsWith("//") || line.isEmpty()) {
						put(null, line);
					} else {
						String[] split = line.split("\\s*=\\s*", 2);
						if (split.length == 2) {
							put(split[0], split[1]);
						} else if (split.length == 1 && !split[0].isEmpty()) {
							throw new IOException("Config line " + iLine + " invalid: " + split[0]);
						}
					}
					line = reader.readLine();
				}
			} finally {
				reader.close();
				inputStreamReader.close();
				bufferedInputStream.close();
				fis.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
			_configChanged = true;
		}
	}
	
	private KeyValue put(String key, Object value) {
		if (key == null) {
			KeyValue keyValue = new KeyValue(null, value);
			_data.add(keyValue);
			_configChanged = true;
			return keyValue;
		}
		for (KeyValue kv : _data) {
			if(key.equals(kv.key)) {
				kv.value = value;
				_configChanged = true;
				return kv;
			}
		}
		KeyValue newEntry = new KeyValue(key, value);
		_data.add(newEntry);
		_configChanged = true;
		return newEntry;
	}
	
	private KeyValue get(String key) {
		for (KeyValue kv : _data) {
			if(key.equals(kv.key)) {
				return kv;
			}
		}
		return null;
	}
	
	public String getString(String key, String defaultValue) {
		KeyValue kv = get(key);
		if (kv == null) {
			kv = new KeyValue(key, defaultValue);
			_data.add(kv);
			_configChanged = true;
		}
		return kv.value.toString();
	}
	
	public List<KeyValue> getAll(String keyPrefix) {
		List<KeyValue> result = new ArrayList<KeyValue>();
		for (KeyValue e : _data) {
			if ((e.key != null) && e.key.startsWith(keyPrefix)) {
				result.add(e);
			}
		}
		return result;
	}
	
	private <T> KeyValue getDefault(String key, T defaultValue) {
		KeyValue keyValue = get(key);
		if (keyValue == null) {
			keyValue = new KeyValue(key, defaultValue);
			_data.add(keyValue);
			_configChanged = true;
		}
		return keyValue;
	}
	
	public int getInt(String key, int defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Integer.parseInt(keyValue.value.toString());
		}
		if (keyValue.value instanceof Integer) {
			return (Integer)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (int)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public long getLong(String key, long defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Long.parseLong(keyValue.value.toString());
		}
		if (keyValue.value instanceof Long) {
			return (Long)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (long)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public float getFloat(String key, float defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Float.parseFloat(keyValue.value.toString());
		}
		if (keyValue.value instanceof Float) {
			return (Float)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (float)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Boolean.parseBoolean(keyValue.value.toString());
		}
		if (keyValue.value instanceof Boolean) {
			return (Boolean)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (boolean)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public int getColor(String key, int defaultValue) {
		KeyValue keyValue = getDefault(key, new Color(defaultValue));
		if (keyValue.value instanceof String) {
			long intVal = Long.parseLong(keyValue.value.toString(), 16);
			keyValue.value = new Color((int)intVal);
		}
		if (keyValue.value instanceof Color) {
			return ((Color)keyValue.value).argb;
		}
		throw new IllegalStateException("Invalid config type for (color)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public void write(boolean force) {
		if (_configChanged || force) {
			try {
				FileOutputStream fos = new FileOutputStream(new File("config.ini"));
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream);
				BufferedWriter writer = new BufferedWriter(outputStreamWriter);
				try {
					for (KeyValue data : _data) {
						if (data.key == null) {
							writer.write(data.value + "\r\n");
						} else if (data.value instanceof Color) {
							Color c = (Color) data.value;
							writer.write(data.key + " = " + String.format("%08X", c.argb) + "\r\n");
						} else {
							writer.write(data.key + " = " + data.value + "\r\n");
						}
					}
				} finally {
					writer.close();
					outputStreamWriter.close();
					bufferedOutputStream.close();
					fos.close();
				}
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class Color {
		public final int argb;
		
		public Color(final int argb) {
			this.argb = argb;
		}
	}
}
