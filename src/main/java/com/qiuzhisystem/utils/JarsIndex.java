package com.qiuzhisystem.utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

	private static Logger logger=Logger.getLogger(JarsIndex.class);
	
	private static Connection con=null;
	
	public static void main(String[] args) throws IOException{
		DbUtil dbUtil=new DbUtil();
		try {
			con=dbUtil.getConn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Directory dir=FSDirectory.open(Paths.get(PropertiesUtil.getValue("indexFile")));
		Analyzer analyzer=new StandardAnalyzer();
		IndexWriterConfig conf=new IndexWriterConfig(analyzer);
		IndexWriter writer=new IndexWriter(dir, conf);
		
		String sql="select * from t_jar where indexState=0";
		try{
			PreparedStatement pstmt=con.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()){
				String id=rs.getString("uuid");
				String name=rs.getString("name");
				Document doc=new Document();
				doc.add(new StringField("id",id,Field.Store.YES));
				doc.add(new TextField("name",name.replaceAll("-", " "),Field.Store.YES));
				writer.addDocument(doc);
				
				// �������ݿ�indexState״̬�ֶ� �ĳ�1
				String sql2="update t_jar set indexState=1 where uuid='"+id+"'";
				PreparedStatement pstmt2=con.prepareStatement(sql2);
				pstmt2.executeUpdate();
			}
		}catch(Exception e){
		}
		try {
			dbUtil.closeCon(con);
		} catch (Exception e) {
			logger.error("Exception", e);
		}
		writer.close(); // �ر�д��
	}
}
