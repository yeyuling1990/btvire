package com.ctvit.nlp.userportrait.tfidf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * mysql连接管理类
 * 
 * @author zhilin zhang
 * @date   2017年10月27日下午4:43:30
 * @version 1.0
 */
public class DbConnections {
	private static Logger logger = LoggerFactory.getLogger(DbConnections.class);
	private static Properties properties = new Properties();
	private static String mysql_url = "";
	private static String mysql_username = "";
	private static String mysql_password = "";
	
	static {
		String configFilePath = "/db.properties";
		try {
			properties.load(DbConnections.class.getResourceAsStream(configFilePath));
			Class.forName("com.mysql.jdbc.Driver");
			 mysql_url = properties.getProperty("mysql_url");
			 mysql_username = properties.getProperty("mysql_username");
			 mysql_password = properties.getProperty("mysql_password");
		} catch (IOException e) {
			logger.error("加载配置文件出错,path= " + configFilePath, e);
		} catch (ClassNotFoundException e) {
			logger.error("加载数据库驱动出错", e);
		}

	}
	
	/**
	 * 获取内容数据对应的数据库连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getContentDataConnection() throws SQLException {
		return DriverManager.getConnection(mysql_url, mysql_username, mysql_password);
	}
}
