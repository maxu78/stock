package com.mx.stock;

import com.mx.stock.job.StockSpider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;


@Slf4j
@SpringBootTest
class StockApplicationTests {

	@Autowired
	private StockSpider stockSpider;

	@Test
	void contextLoads() {

	}

	@Test
	public void test() {
		long start = System.currentTimeMillis();
		boolean available = stockSpider.isIndexAvailable(StockSpider.INDEX);
		if(!available) {
			String index = stockSpider.createIndex(StockSpider.INDEX);
			log.info("createIndex: " + StockSpider.INDEX + " , result: " + index);
		}
		long start0 = System.currentTimeMillis();
		log.info("check and create index costs: " + (start0 - start));
		long start1 = System.currentTimeMillis();
		String result = stockSpider.deleleByQuery("000001");
		long start2 = System.currentTimeMillis();
		log.info("delete costs: " + (start2 - start1));
		stockSpider.executeCsv(new File("E:/000001.csv"), new File("E:/000001.csv"), new File("E:/000001.csv"), new File("E:/000001.csv"), new File("E:/000001.csv"));
		log.info("bulk costs: " + (System.currentTimeMillis() - start2));
	}
}
