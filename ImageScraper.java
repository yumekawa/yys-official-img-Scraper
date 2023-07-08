import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ImageScraper {
    public static void main(String[] args) {
        // yys官方图片下载
        String url = "https://yys.163.com/media/picture.html";

        try {
            // 发送HTTP请求并获取HTML页面
            Document doc = Jsoup.connect(url).get();

            // 选择所有的<div>标签 with class "mask"
            Elements divElements = doc.select("div.mask");

            // 保存指定格式的图片链接地址到文本并按分辨率分类
            saveSortedUniqueImageLinksByResolution(divElements);

            System.out.println("图片链接保存完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveSortedUniqueImageLinksByResolution(Elements divElements) throws IOException {
        Map<String, Set<String>> imageLinksByResolution = new HashMap<>();

        // 遍历每个<div>标签
        for (Element divElement : divElements) {
            // 选择<a>标签
            Elements aElements = divElement.select("a");

            // 遍历每个<a>标签，获取链接地址并保存到对应分辨率的集合中
            for (Element element : aElements) {
                // 获取链接的URL
                String linkUrl = element.attr("href");

                // 检查链接地址是否符合指定格式
                if (isValidImageUrl(linkUrl)) {
                    String resolution = extractResolutionFromUrl(linkUrl);
                    if (!resolution.isEmpty()) {
                        imageLinksByResolution.computeIfAbsent(resolution, k -> new TreeSet<>()).add(linkUrl);
                    }
                }
            }
        }

        // 将链接地址按分辨率分类写入文本
        for (Map.Entry<String, Set<String>> entry : imageLinksByResolution.entrySet()) {
            String resolution = entry.getKey();
            Set<String> links = entry.getValue();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resolution + "_image_links.txt"))) {
                for (String link : links) {
                    writer.write(link);
                    writer.newLine();
                }
            }
        }
    }

    private static boolean isValidImageUrl(String imgUrl) {
        return imgUrl.startsWith("https://yys.res.netease.com/pc/zt/20230421141009/data/picture/")
                && (imgUrl.endsWith(".png") || imgUrl.endsWith(".jpg") || imgUrl.endsWith(".jpeg") || imgUrl.endsWith(".gif"));
    }

    private static String extractResolutionFromUrl(String imgUrl) {
        int startIdx = imgUrl.lastIndexOf('/');
        int endIdx = imgUrl.lastIndexOf('.');

        if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
            String resolution = imgUrl.substring(startIdx + 1, endIdx);
            if (resolution.matches("\\d+x\\d+")) {
                return resolution;
            }
        }

        return "";
    }
}
