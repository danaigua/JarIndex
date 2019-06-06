package com.qiuzhisystem.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.qiuzhisystem.utils.DbUtil;
import com.qiuzhisystem.utils.PropertiesUtil;

public class JarsIndex {
	
	private static Logger logger = Logger.getLogger(JarsIndex.class);
	
	private static Connection con = null;
	
	public static void main(String[] args) throws IOException {
		logger.info("创建索引开始");
		DbUtil dbUtil = new DbUtil();
		try {
			con = dbUtil.getConn();
			logger.info("创建数据库连接成功");
		} catch (Exception e) {
			logger.info("创建数据库连接失败");
			e.printStackTrace();
		}
		//实例化索引类
		Directory dir = FSDirectory.open(Paths.get(PropertiesUtil.getValue("indexFile")));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(dir, conf);
		
		String sql = "select * from t_jar where indexState=0";
		PreparedStatement pstmt;
		try {
			pstmt = con.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				String id = rs.getString("uuid");
				String name = rs.getString("name");
				Document document = new Document();
				document.add(new StringField("id", id, Field.Store.YES));
				document.add(new TextField("name", name.replaceAll("-", " "), Field.Store.YES));
				writer.addDocument(document);
				
				//更新数据库indexState状态字段，改成1
				String sql2 = "update t_jar set indexState = 1 where uuid = '" + id + "'";
				PreparedStatement pstmt2 = con.prepareStatement(sql2);
				pstmt2.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			dbUtil.closeCon(con);
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.close();
		logger.info("创建索引完成");
	}

}
