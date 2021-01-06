package com.mx.stock.job;

import com.mx.stock.entity.StockBaseEntity;
import com.mx.stock.entity.StockSummaryEntity;
import com.mx.stock.enums.StockTypeEnum;
import com.mx.stock.util.HttpTemplate;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.boot.BBossESStarter;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class StockSpider {

    /*文件下载目录*/
    public static final String DATA_DIR = "/usr/local/data/";

    /*文件下载路径*/
    public static final String DATA_PATH = DATA_DIR + "${code}.csv";

    /*获取新浪股票代码地址*/
    public static final String STOCK_CODE_URL = "http://app.finance.ifeng.com/list/stock.php?t=hs&f=symbol&o=asc&p=${p}";

    /*获取东方财富股票历史数据地址*/
    public static final String DATA_URL = "http://quotes.money.163.com/service/chddata.html?code=${type}${code}&start=${start}&end=${end}&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";



    public static final String INDEX = "stock_summary";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HttpTemplate httpTemplate;

    @Autowired
    private BBossESStarter bbossESStarter;

    static {
        //check 文件目录
        File file = new File(DATA_DIR);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    @Scheduled(cron = "0 20 20 ? * MON-FRI")
    public void executeHistory() {
        log.info("start");
        long start = System.currentTimeMillis();
        List<StockBaseEntity> stockCodeList;

        try {
            stockCodeList = getStockCodeList();
        } catch (Exception e) {
            try {
                stockCodeList = getStockCodeList();
            } catch (Exception e1) {
                try {
                    stockCodeList = getStockCodeList();
                } catch (Exception e2) {
                    try {
                        stockCodeList = getStockCodeList();
                    } catch (Exception e3) {
                        log.error("query stock code get error, ", e);
                        return;
                    }
                }
            }
        }
        log.info("query stockCode costs: " + (System.currentTimeMillis() - start));
        long cp = System.currentTimeMillis();
        boolean available = isIndexAvailable(INDEX);
        if(!available) {
            String index = createIndex(INDEX);
            log.info("createIndex: " + INDEX + " , result: " + index);
        }
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        log.info(day);
        for(StockBaseEntity entity : stockCodeList) {
            try {
                long cp1 = System.currentTimeMillis();
                File csvFile = getStockHistoryInfo(entity.getCode(), "", day, entity.getType());
                String result = deleleByQuery(entity.getCode());
                log.info("delete: " + entity.getCode() + ", result: " + result);
                //读取data数据入到es中
                executeCsv(csvFile);
                log.info("save code: " + entity.getCode() + " costs: " + (System.currentTimeMillis() - cp1));
            } catch (Exception e) {
                log.error("", e);
            }
        }
        log.info("end, and save info costs: " + (System.currentTimeMillis() - cp));
        log.info("get history costs: " + (System.currentTimeMillis() - start));
    }

    public boolean executeCsv(File... file) {
        CSVReader csvReader = null;
        if(file == null) {
            return false;
        }
        List<File> fileList = Arrays.asList(file);
        try {
            ClientInterface restClient = bbossESStarter.getRestClient();

            List<StockSummaryEntity> entityList = new ArrayList<>();
            StringBuilder fileNames = new StringBuilder();
            int count = 0;
            for(File f : fileList) {
                fileNames.append(f.getName()).append(", ");
                csvReader = new CSVReader(new InputStreamReader(new FileInputStream(f), "GB2312"));
                String[] strs;
                int i = 0;
                while ((strs = csvReader.readNext()) != null) {
                    if(i++ == 0) {
                        continue;
                    }
                    StockSummaryEntity entity = new StockSummaryEntity();
                    entity.setDay(strs[0]);
                    entity.setDayStamp(new SimpleDateFormat("yyyy-MM-dd").parse(strs[0]).getTime());
                    String code = strs[1].startsWith("'") ? strs[1].substring(1) : strs[1];
                    entity.setCode(code);
                    entity.setRouting(code);
                    entity.setType(StockTypeEnum.getStockType(code).getCode());
                    entity.setName(strs[2]);
                    entity.setTClose(parseDouble(strs[3]));
                    entity.setHigh(parseDouble(strs[4]));
                    entity.setLow(parseDouble(strs[5]));
                    entity.setTOpen(parseDouble(strs[6]));
                    entity.setLClose(parseDouble(strs[7]));
                    entity.setChg(parseDouble(strs[8]));
                    entity.setPchg(parseDouble(strs[9]));
                    entity.setTurnOver(parseDouble(strs[10]));
                    entity.setVoTurnOver(parseSpecialLong(strs[11]));
                    entity.setVaTurnOver(parseSpecialLong(strs[12]));
                    entity.setTCap(parseSpecialLong(strs[13]));
                    entity.setMCap(parseLong(strs[14]));
                    entityList.add(entity);
                }
                count += i;
            }
            String s = restClient.addDocuments(INDEX, entityList);
//            log.info("bulk " + fileNames + " result: " + s);
            fileList.forEach(File::delete);
            log.info("file: " + fileNames + " execute ends, count: " + (count - fileList.size()));
        } catch (Exception e) {
            log.error("", e);
            return false;
        } finally {
            try {
                if (csvReader != null) {
                    csvReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public String deleleByQuery(String code) {
        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/QueryStockSummary.xml");
        StockSummaryEntity entity = new StockSummaryEntity();
        entity.setCode(code);
        return clientUtil.deleteByQuery(INDEX + "/_delete_by_query?routing=" + code, "queryByCode", entity);
    }

    public File getStockHistoryInfo(String code, String start, String end, String type) throws Exception {

        if("sz".equals(type)) {
            type = "1";
        } else if("sh".equals(type)) {
            type = "0";
        } else {
            log.error("error");
            return null;
        }
        File file = null;
        String url = DATA_URL.replace("${type}", type).replace("${code}", code).replace("${start}", start).replace("${end}", end);
        ResponseEntity<Resource> entity = restTemplate.getForEntity(url, Resource.class);
        if(HttpStatus.OK.equals(entity.getStatusCode())) {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                Resource body = entity.getBody();
                if(body != null) {
                    in = body.getInputStream();
                    file = new File(DATA_PATH.replace("${code}", code));
                    out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int ch;
                    while ((ch = in.read(buffer)) != -1) {
                        out.write(buffer, 0, ch);
                    }
                    out.flush();
                }
            } finally {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.close();
                }
            }
        } else {
            log.error(entity.getStatusCodeValue() + "");
        }
        return file;
    }

    public List<StockBaseEntity> getStockCodeList() throws Exception {

        List<StockBaseEntity> result = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            String url = STOCK_CODE_URL.replace("${p}", String.valueOf(i));
            Document document = Jsoup.parse(new URL(url), 10000);
            Elements trElement = document.select(".tab01 table tr");
            if (trElement != null && trElement.size() > 2) {
                for (Element element : trElement) {
                    Elements tdElement;
                    if ((tdElement = element.select("td")) != null && tdElement.size() >= 2) {
                        Element codeElement = tdElement.get(0);
                        Element nameElement = tdElement.get(1);
                        StockBaseEntity entity = new StockBaseEntity();
                        entity.setCode(codeElement.text());
                        entity.setName(nameElement.text());
                        entity.setType(StockTypeEnum.getStockType(entity.getCode()).getCode());
                        result.add(entity);
                    }
                }
            } else {
                break;
            }
        }
        return result;
    }

    public boolean isIndexAvailable(String index) {
        return bbossESStarter.getRestClient().existIndice(index);
    }

    public String createIndex(String index) {
        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/CreateIndex.xml");
        return clientUtil.createIndiceMapping(index, "createIndex");
    }

    public String dropIndex(String index) {
        String indice = bbossESStarter.getRestClient().dropIndice(index);
        return indice;
    }

    public static void main(String[] args) {

        new StockSpider().executeCsv(new File("C:\\Users\\maxu\\Downloads\\601857.csv"));
    }

    private long parseLong(String s) {
        try {
            return Double.valueOf(s).longValue();
        } catch (Exception ignore) {

        }
        return 0L;
    }

    private long parseSpecialLong(String s) {
        try {
            BigDecimal bigDecimal = new BigDecimal(s);
            return parseLong(bigDecimal.toPlainString());
        } catch (Exception ignore) {

        }
        return 0L;
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ignore) {

        }
        return 0D;
    }

    private double parseSpecialDouble(String s) {
        try {
            BigDecimal bigDecimal = new BigDecimal(s);
            return parseDouble(bigDecimal.toPlainString());
        } catch (Exception ignore) {

        }
        return 0D;
    }
}
