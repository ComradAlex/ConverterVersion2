
import com.jcraft.jsch.*;

public class Sftp {
    private static long time0;

    public static class Downloader {                            //реализуем функционал, необходимый для скачивания файла с удалённого сервера

        public static void download(String host, Integer port, String user, String password, String localFile, String sourceFile) {
            try {
                JSch jsch = new JSch();

                Session session = jsch.getSession(user, host, port);        //создаётся сессия
                session.setUserInfo(new MyUserInfo(password));              //указывается UserInfo
                session.connect();

                System.out.println("session connected");

                Channel channel = session.openChannel("sftp");              //если получилось установить сессию, то открываем защищённый канал sftp для передачи файлов
                channel.connect();

                System.out.println("channel connected");

                ChannelSftp channelSftp = (ChannelSftp) channel;

                try {
                    System.out.println("start");                            //если всё успешно, начинаем скачивание файла с удалённого сервера
                    time0 = System.currentTimeMillis();
                    channelSftp.get(sourceFile, localFile, new MyProgressMonitor(sourceFile), ChannelSftp.OVERWRITE);  //переписываем файл, если на локальном компьютере файл с таким именем уже есть
                } catch (SftpException cause) {
                    cause.printStackTrace();
                }

                channelSftp.exit();

                session.disconnect();
            } catch (Exception cause) {
                cause.printStackTrace();
            }
        }
    }


    public static class Uploader {                              //

        public static void upload(String host, Integer port, String user, String password, String localFile, String destinationFile) {
            try {
                JSch jsch = new JSch();

                Session session = jsch.getSession(user, host, port);
                session.setUserInfo(new MyUserInfo(password));
                session.connect();

                System.out.println("session connected");

                Channel channel = session.openChannel("sftp");
                channel.connect();

                System.out.println("channel connected");

                ChannelSftp channelSftp = (ChannelSftp) channel;

                try {
                    System.out.println("start");
                    time0 = System.currentTimeMillis();
                    channelSftp.put(localFile, destinationFile, new MyProgressMonitor(destinationFile), ChannelSftp.OVERWRITE);
                } catch (SftpException cause) {
                    cause.printStackTrace();
                }

                channelSftp.exit();

                session.disconnect();
            } catch (Exception cause) {
                cause.printStackTrace();
            }
        }
    }


    private static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        private String password;

        public MyUserInfo(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return new String[] { password };
        }
    }


    private static class MyProgressMonitor implements SftpProgressMonitor {     //позволяет видеть в процентах, каков прогресс скачивания файла
        private String sourceFile;

        private long count;
        private long max;
        private long percent;

        public MyProgressMonitor(String sourceFile) {
            this.sourceFile = sourceFile;
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            this.max = max;
            this.count = 0;                                                     //счётчик, хранит общее значение считанных байт
            this.percent = -1;
        }

        @Override
        public boolean count(long count) {                                      //получает количество переданных байтов при чтении файла
            this.count += count;

            if (percent >= this.count * 100 / max) {
                return true;
            }

            percent = this.count * 100 / max;

            System.out.println("progress: " + percent + " %");

            return true;
        }

        @Override
        public void end() {
            System.out.println("progress: finished in " + (System.currentTimeMillis() - time0) + "ms");
        }
    }
}
