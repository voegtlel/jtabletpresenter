package de.freiburg.uni.tablet.presenter.document;

import java.awt.Color;
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
		
		public KeyValue(final String key, final Object value) {
			this.key = key;
			this.value = value;
		}
	}
	
	private List<KeyValue> _data = new ArrayList<KeyValue>();
	private boolean _configChanged = false;
	
	public DocumentConfig(final String filename) {
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
	
	private KeyValue put(final String key, final Object value) {
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
	
	private KeyValue get(final String key) {
		for (KeyValue kv : _data) {
			if(key.equals(kv.key)) {
				return kv;
			}
		}
		return null;
	}
	
	public void setString(final String key, final String value) {
		KeyValue kv = get(key);
		if (kv == null) {
			kv = new KeyValue(key, value);
			_data.add(kv);
			_configChanged = true;
		} else {
			_configChanged = !kv.value.equals(value);
			kv.value = value;
		}
	}
	
	public String getString(final String key, final String defaultValue) {
		KeyValue kv = get(key);
		if (kv == null) {
			if (defaultValue == null) {
				return null;
			}
			kv = new KeyValue(key, defaultValue);
			_data.add(kv);
			_configChanged = true;
		}
		return kv.value.toString();
	}
	
	public List<KeyValue> getAll(final String keyPrefix) {
		List<KeyValue> result = new ArrayList<KeyValue>();
		for (KeyValue e : _data) {
			if ((e.key != null) && e.key.startsWith(keyPrefix)) {
				result.add(e);
			}
		}
		return result;
	}
	
	private <T> KeyValue getDefault(final String key, final T defaultValue) {
		KeyValue keyValue = get(key);
		if (keyValue == null) {
			if (defaultValue == null) {
				return null;
			}
			keyValue = new KeyValue(key, defaultValue);
			_data.add(keyValue);
			_configChanged = true;
		}
		return keyValue;
	}
	
	public int getInt(final String key, final int defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Integer.parseInt(keyValue.value.toString());
		}
		if (keyValue.value instanceof Integer) {
			return (Integer)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (int)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public long getLong(final String key, final long defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Long.parseLong(keyValue.value.toString());
		}
		if (keyValue.value instanceof Long) {
			return (Long)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (long)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public float getFloat(final String key, final float defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Float.parseFloat(keyValue.value.toString());
		}
		if (keyValue.value instanceof Float) {
			return (Float)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (float)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public boolean getBoolean(final String key, final boolean defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			keyValue.value = Boolean.parseBoolean(keyValue.value.toString());
		}
		if (keyValue.value instanceof Boolean) {
			return (Boolean)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (boolean)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public Color getColor(final String key, final Color defaultValue) {
		KeyValue keyValue = getDefault(key, defaultValue);
		if (keyValue.value instanceof String) {
			long intVal = Long.parseLong(keyValue.value.toString(), 16);
			keyValue.value = new Color((int)((intVal >> 16) & 0xff), (int)((intVal >> 8) & 0xff), (int)((intVal >> 0) & 0xff), (int)((intVal >> 24) & 0xff));
		}
		if (keyValue.value instanceof Color) {
			return (Color)keyValue.value;
		}
		throw new IllegalStateException("Invalid config type for (color)" + key + ": " + keyValue.value.getClass().getName());
	}
	
	public void write(final boolean force) {
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
							writer.write(data.key + " = " + String.format("%08X", c.getRGB() | (c.getAlpha() << 24)) + "\r\n");
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
}
