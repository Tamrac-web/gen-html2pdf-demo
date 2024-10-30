package com.example.pdf.demos;



import com.example.pdf.demos.web.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;


@Controller
@Slf4j
public class PdfController {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @GetMapping("/generate-pdf")
    public void generatePdf(HttpServletResponse response) {
        try {
            // 1. 准备数据
            Context context = new Context();
            context.setVariable("title", "这是PDF标题 This is PDF Title");
            context.setVariable("content", "这是PDF内容 This is PDF Content");

            ArrayList<User> userList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                User user = new User();
                user.setName("张三" + i);
                user.setAge(i);
                userList.add(user);
            }
            context.setVariable("userList", userList);

            // 2. 渲染HTML
            String htmlContent = templateEngine.process("template", context);
            log.info("htmlContent: {}", htmlContent);

            // 3. 转换为 XHTML（OpenHTMLtoPDF 可以直接处理 HTML，但确保 HTML 结构正确）
            String xhtml = convertToXhtml(htmlContent);


            // 4. 转换为 PDF
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();

            // 设置 HTML 内容和资源路径（确保图片等资源可以被访问）
            String resourcePath = PdfController.class.getResource("/static/").toString();
            builder.withHtmlContent(xhtml, resourcePath);

            //添加字体库 支持中文
            builder.useFont(new File("src/main/resources/static/fonts/NotoSansSC-Regular.ttf"), "NotoSansSC");


            // 设置输出流
            builder.toStream(os);

            // 启用快速模式（可选）
            builder.useFastMode();

            // 运行转换
            builder.run();

            // 5. 设置响应头
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"document.pdf\"");
            response.setContentLength(os.size());

            // 6. 写入响应
            os.writeTo(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            // 处理异常
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("生成 PDF 失败", e);
        }
    }


    /**
     * 将 HTML 转换为 XHTML
     * @param html HTML 内容
     * @return XHTML 内容
     */
    private String convertToXhtml(String html) {
        // Flying Saucer 需要 XHTML 格式的 HTML
        // 可以使用 JSoup 或其他库来转换
        org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);
        jsoupDoc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        return jsoupDoc.html();
    }
}
