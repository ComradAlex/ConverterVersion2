
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;

public class main {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) throws IOException {

        String sourceHost     = "host1.com";                                                                      //параметры нашего пользователя
        Integer sourcePort    = 22;
        String sourceUser     = "user1";
        String sourcePassword = "password1";
        String sourceFile     = "Пример.pdf";            //файл-источник

        String localFile = "/home/catalog";      //директория для скачивания

        String destinationHost     = "host2.com";                                                                 //параметры удалённого сервера
        Integer destinationPort    = 22;
        String destinationUser     = "user2";
        String destinationPassword = "password2";
        String destinationPath     = "/home/user2/";


        try {
            Sftp.Downloader.download(sourceHost, sourcePort, sourceUser, sourcePassword, localFile, sourceFile);  //с помощью метода download скачиваем исходный файл в формате pdf

            PDDocument document = PDDocument.loadNonSeq(new File(sourceFile), null);

            List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();                                   //используя библиотеку pdfbox, получаем список из страниц исходного документа

            int page = 0;                                                                                         //вводим счётчик для номера страницы

            for(PDPage pdPage : pdPages){
                ++page;
                BufferedImage bim = pdPage.convertToImage(BufferedImage.TYPE_INT_RGB, 300);                       //в цикле проходим по всем страницам документа, каждую преобразуем в формат jpg с качеством 300 dpi
                ImageIOUtil.writeImage(bim, sourceFile+"-"+page+".jpg", 300);
                Sftp.Uploader.upload(destinationHost, destinationPort, destinationUser, destinationPassword, sourceFile+"-"+page+".jpg", destinationPath+"-"+page+".jpg"); //с помощью метода upload загружаем полученные изображения в формате jpg на сервер
            }



        } catch (Throwable cause) {
            cause.printStackTrace();
        }



    }

}
