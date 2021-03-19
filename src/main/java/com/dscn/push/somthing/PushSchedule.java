package com.dscn.push.somthing;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpHost;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.Zhao
 * @createTime 2021/3/17 10:57
 */
@Component
public class PushSchedule {
    private String temp=File.separator+"DataDisk"+File.separator+"rePush";

    @Scheduled(cron = "0 0 8,9,10,11,12,13,14,15,16,17,18,19,20,21,22 * * ? ")
    public void rushSome() {
        int count=1;
        int max=10000;
        int min=60000;
        System.out.println("push-----------begin-----------");
        File urlFile = new File(temp+File.separator+"url.txt");
        File deviceFile = new File(temp+File.separator+"device.txt");
        String proxyUrl = "http://82.156.111.64:5010/get_all/";
        List<String> proxyList = new ArrayList<>();
        List<String> urlList = new ArrayList<>();
        List<String> agent = new ArrayList<>();
        Unirest.setTimeouts(10000,10000);
        getProxy(proxyList, proxyUrl);
        int m = 0;
        while (proxyList.size() == 0 && m < 3) {
            m++;
            getProxy(proxyList, proxyUrl);
        }
        getSomething(urlList, urlFile);
        getSomething(agent, deviceFile);
        for (String age : agent) {
            if (proxyList.size() > 0) {
                for (String proxy : proxyList) {
                    String[] split = proxy.split(":");
                    HttpHost httpHost = new HttpHost(split[0], Integer.parseInt(split[1]));
                    Unirest.setProxy(httpHost);
                    sendSomething(urlList, age);
                }
            } else {
                Unirest.setProxy(null);
                sendSomething(urlList, age);
                System.out.println("第<"+count+">此循环结束");
                count++;
            }
            try {
                long round = Math.round(Math.floor(Math.random() * (max - min) + min));
                Thread.sleep(round);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }



    }

    private void getProxy(List<String> proxyList, String proxyUrl) {
        try {
            HttpResponse<String> re = null;
            re = Unirest.get(proxyUrl).asString();
            String body = re.getBody();
            JSONArray objects = JSONArray.parseArray(body);
            for (Object object : objects) {
                JSONObject obj = (JSONObject) object;
                String proxy = (String) obj.get("proxy");
                proxyList.add(proxy);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }

    private void sendSomething(List<String> urlList, String age) {
        for (String url : urlList) {
            HttpResponse<String> stringHttpResponse = null;
            try {
                stringHttpResponse = Unirest.get(url)
                        .header("User-Agent", age)
                        .asString();
                String bodys = stringHttpResponse.getBody();
                System.out.println("成功：--"+url+"-:"+age);
            } catch (Exception e) {
                System.out.println("失败：--"+url+"-:"+age);
                e.printStackTrace();

            }

        }
    }

    private void getSomething(List<String> list, File file) {
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {
            String url = null;
            while ((url=reader.readLine() )!= null) {
                list.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
