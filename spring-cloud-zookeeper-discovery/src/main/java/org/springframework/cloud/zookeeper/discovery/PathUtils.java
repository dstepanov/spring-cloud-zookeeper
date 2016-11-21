package org.springframework.cloud.zookeeper.discovery;

/**
 * @author Denis Stepanov
 */
public class PathUtils {

	public static String sanitize(String path) {
		return withLeadingSlash(withoutSlashAtEnd(path));
	}

	private static String withLeadingSlash(String value) {
		return value.startsWith("/") ? value : "/" + value;
	}

	private static String withoutSlashAtEnd(String value) {
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

}
