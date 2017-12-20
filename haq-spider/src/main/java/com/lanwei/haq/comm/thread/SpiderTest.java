package com.lanwei.haq.comm.thread;

import com.lanwei.haq.comm.util.PropertiesUtil;
import com.lanwei.haq.comm.util.SpiderUtil;
import com.lanwei.haq.spider.entity.NewsEntity;
import com.lanwei.haq.spider.entity.web.WebEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @description：
 * @author：liuxinyun
 * @date：2017/12/15 22:31
 */
public class SpiderTest implements Callable {

    private WebEntity webEntity;

    public SpiderTest(WebEntity webEntity) {
        this.webEntity = webEntity;
    }

    @Override
    public Object call() throws Exception {
        StringBuilder sb = new StringBuilder();
        Document document = null;
        document = getDocument(webEntity.getWeburl());
        if (null == document) {
            sb.append("网址:").append(webEntity.getWeburl()).append("连接有误，获取不到页面.");
        }
        //获取所有子链接
        Set<String> suburls = SpiderUtil.getLinks(document);
        Set<String> noRegex = new HashSet<>(); //存储不符合正则表达式
        boolean flag = false; //标记，爬取到一个符合正则表达式的即可
        for (String suburl : suburls) {
            if (!SpiderUtil.matchUrl(webEntity.getRegex(), suburl)){
                noRegex.add(suburl);
                continue;
            }
            if (flag) {
                continue;
            }
            document = getDocument(suburl);
            if (null == document) {
                continue;
            }
            NewsEntity newsByDoc = SpiderUtil.getNewsByDoc(document, webEntity.getTitleSelect(), webEntity.getContentSelect());
            sb.append("其中一个符合正则表达式的爬取内容为：&#13;&#10;");
            sb.append("网站:").append(suburl).append("&#13;&#10;&#13;&#10;")
                    .append("标题:").append(newsByDoc.getTitle()).append("&#13;&#10;&#13;&#10;")
                    .append("内容:").append(newsByDoc.getContent()).append("&#13;&#10;&#13;&#10;");
            flag = true;
        }
        if (noRegex.size() == suburls.size()){
            sb.append("网页所有连接均不符合正则表达式，请检查。&#13;&#10;");
        }
        sb.append("&#13;&#10;&#13;&#10;不符合正则表达式:&#13;&#10;");
        for (String s : noRegex) {
            sb.append(s).append("&#13;&#10;");
        }
        return sb;
    }

    private Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .proxy(PropertiesUtil.get("proxy.host"), PropertiesUtil.getInt("proxy.port"))
                .ignoreContentType(false)//解析响应是忽略文档类型
                .ignoreHttpErrors(false)  //响应时是否忽略错误，404等
                .validateTLSCertificates(false)//关闭证书验证
                .timeout(3000).get();
    }

}
