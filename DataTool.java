import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * General purpose class to deal with reading/writing data from/to files.
 */
public final class DataTool {
    public static final String SEPARATOR = System.getProperty("line.separator");
    public static final String TRUE = "true";

    /**
     * Do not allow objects of this class to be made.
     */
    private DataTool() {
    }

    /**
     * Adds an addition to a string if that addition is not already in the string.
     *
     * @param source    the source string
     * @param addition  the additional string
     * @param separator the separator
     * @return the source string without the duplicate or null
     */
    public static String addNonDuplicate(String source, String addition, String separator) {
        if(source == null) {
            return null;
        }
        else if(source.equals("")) { // source string was empty
            return addition;
        }
        else if(!source.contains(addition)) { // source string did not have the addition
            return source + separator + addition;
        }
        else { // source string did have the addition
            return source;
        }
    }

    /**
     * Adds an addition to a string if that addition is not already in the string.
     *
     * @param source    the source string
     * @param addition  the additional string
     * @param separator the separator
     * @return the source string without the duplicate or null
     */
    public static String addNonDuplicateIgnoresCase(String source, String addition, String separator) {
        if(source == null) {
            return null;
        }
        else if(source.equals("")) { // source string was empty
            return addition;
        }
        else if(!source.toLowerCase().contains(addition.toLowerCase())) { // source string did not have the addition
            return source + separator + addition;
        }
        else { // source string did have the addition
            return source;
        }
    }

    /**
     * Creates folders/files.
     *
     * @param filenames the file names
     */
    public static void createFiles(List<String> filenames) {
        if(filenames == null) {
            return;
        }

        for(String filename : filenames) {
            if(filename.endsWith("\\") || filename.endsWith("/")) {
                File folder = new File(filename);
                folder.mkdir();
            }
            else {
                File file = new File(filename);
                try {
                    file.createNewFile();
                }
                catch(IOException e) {
                }
            }
        }
    }

    /**
     * Creates folders/files.
     *
     * @param filenames the file names
     */
    public static void deleteFiles(List<String> filenames) {
        if(filenames == null) {
            return;
        }

        List<String> foldernames = new ArrayList<>();

        // delete all files in folders
        for(String filename : filenames) {
            if(filename.endsWith("\\") || filename.endsWith("/")) {
                foldernames.add(filename);
            }
            else {
                File file = new File(filename);
                file.delete();
            }
        }

        // delete folders backwards
        for(int i = foldernames.size() - 1; i >= 0; i--) {
            File folder = new File(foldernames.get(i));
            folder.delete();
        }
    }

    /**
     * Deserializes an object from a file.
     *
     * @param filename the filename
     * @return the object on success, otherwise null
     */
    public static Object deserialize(String filename) {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filename))) {
            return objectInputStream.readObject();
        }
        catch(FileNotFoundException e) {
        }
        catch(IOException e) {
        }
        catch(ClassNotFoundException e) {
        }

        return null;
    }

    /**
     * Gets the CRC32 value of the file.
     *
     * @param filename the filename
     * @return the CRC32 value of the file
     */
    public static String getCRC32(String filename) {
        File file = new File(filename);

        return getCRC32(file);
    }

    /**
     * Gets the CRC32 value of the file.
     *
     * @param file the file
     * @return the CRC32 value of the file
     */
    public static String getCRC32(File file) {
        // do not get CRC32 values for folders
        if(file == null || file.isDirectory()) {
            return "";
        }

        // generate CRC32 from file
        String crc32Value = "";
        try {
            InputStream in = new FileInputStream(file);
            CRC32 crc32 = new CRC32();
            byte buffer[] = new byte[2048];
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }
            in.close();

            // leading zeros hack
            String zeroes = "00000000";
            String hex = zeroes + Long.toHexString(crc32.getValue()).toUpperCase();
            crc32Value = hex.substring(hex.length() - zeroes.length());

        }
        catch(FileNotFoundException e) {
        }
        catch(IOException e) {
        }

        return crc32Value;
    }

    /**
     * Gets a boolean list from a list.
     * It expects the data to be in the following format:
     * <title>
     * true
     * false
     * etc
     * </title>
     * <p>
     * or:
     * <title>
     * true
     * false
     * etc
     *
     * @param title the title of the item to find
     * @param data  the list to search through
     * @return the list on success, null on failure
     */
    public static List<Boolean> getDataInTagAsBooleanList(String title, List<String> data) {
        List<String> list = getDataInTagAsList(title, data);
        if(list == null) {
            return null;
        }

        // walk through the list
        List<Boolean> booleanData = new ArrayList<>();
        for(String line : list) {
            if(line.equals(TRUE)) {
                booleanData.add(true);
            }
            else {
                booleanData.add(false);
            }
        }

        return booleanData;
    }

    /**
     * Gets a String list inbetween the opening and closing tags:
     * <tag>
     * data
     * </tag>
     * or:
     * <tag>
     * data
     * // and a blank line with no closing tag in site
     *
     * @param tag  the tag without <> or </> around it
     * @param data the data to be store inbetween the opening and closing tags
     * @return the data inbetween the opening and closing tags
     */
    public static List<String> getDataInTagAsList(String tag, List<String> data) {
        return getDataInTagAsList(tag, tag, data);
    }

    /**
     * Gets a String list inbetween the opening and closing tags:
     * <openingTag>
     * data
     * </closingTag>
     * or:
     * <openingTag>
     * data
     * // and a blank line with no closing tag in site
     *
     * @param openingTag the opening tag without <> around it
     * @param closingTag the closing tag without </> around it
     * @param data       the data to be store inbetween the opening and closing tags
     * @return the data inbetween the opening and closing tags, or null if data is null
     */
    public static List<String> getDataInTagAsList(String openingTag, String closingTag, List<String> data) {
        if(data == null) {
            return null;
        }

        openingTag = "<" + openingTag + ">";
        closingTag = "</" + closingTag + ">";

        List<String> matchedDataWithoutClosingTitle = new ArrayList<>();
        Iterator<String> iterator = data.listIterator();
        while(iterator.hasNext()) {
            String line = iterator.next();

            // find wrapper
            if(line.equals(openingTag)) {
                List<String> matchedData = new ArrayList<>();
                boolean foundBlankLine = false;

                while(iterator.hasNext()) {
                    line = iterator.next();

                    // keep adding data until we find a blank line
                    if(line.equals("")) {
                        foundBlankLine = true;
                    }
                    else if(line.equals(closingTag)) {
                        return matchedData;
                    }

                    // add the line to the corresponding matched data list
                    if(!foundBlankLine) {
                        matchedDataWithoutClosingTitle.add(line);
                    }
                    matchedData.add(line);
                }
            }
        }

        // make sure we found some data
        if(matchedDataWithoutClosingTitle.size() != 0) {
            return matchedDataWithoutClosingTitle;
        }

        return null;
    }

    /**
     * Formats data inbetween the tags:
     * <tag>
     * data
     * </tag>
     *
     * @param tag  the tag without <> or </> around it
     * @param data the data to be store inbetween the tags
     * @return the data with the tags around it
     */
    public static String getDataInTitleTag(String tag, String data) {
        return getDataInTitleTag(tag, tag, data);
    }

    /**
     * Formats data inbetween opening and closing tags:
     * <openingTag>
     * data
     * </closingTag>
     *
     * @param openingTag the opening tag without <> around it
     * @param closingTag the closing tag without </> around it
     * @param data       the data to be store inbetween the opening and closing tags
     * @return the data with the opening and closing tags around it
     */
    public static String getDataInTitleTag(String openingTag, String closingTag, String data) {
        return "<" + openingTag + ">" + SEPARATOR +
                data + SEPARATOR +
                "</" + closingTag + ">";
    }

    /**
     * Converts a list to a string with its elements separated by new line separators.
     *
     * @param list the list
     * @return the separated string list or null
     */
    public static String getListAsString(List<?> list) {
        return getListAsString(list, SEPARATOR);
    }

    /**
     * Converts a list to a string with its elements separated by a separator.
     *
     * @param list      the list
     * @param separator the separator
     * @return the separated string list or null
     */
    public static String getListAsString(List<?> list, String separator) {
        if(list == null) {
            return null;
        }

        String str = "";
        for(Object object : list) {
            str += object + separator;
        }

        str = trimFromTheBack(str, separator);

        return str;
    }

    /**
     * Gets the output from a process.
     *
     * @param command the command to execute
     * @return the output from the process
     */
    public static List<String> getOutputFromProcess(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> list = new ArrayList<>();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
            bufferedReader.close();

            return list;
        }
        catch(IOException e) {
        }

        return null;
    }

    /**
     * Gets the path to remove from a filename.
     *
     * @param file the file
     * @return the path to remove or null
     */
    public static String getPathToRemove(File file) {
        if(file == null) {
            return null;
        }

        String absolutePath = file.getAbsolutePath();

        return absolutePath.substring(0, absolutePath.length() - file.getName().length());
    }

    /**
     * Gets a readable size.
     *
     * @param bytes the size of something
     * @return a human readable version of the size
     */
    public static String getReadableSize(long bytes) {
        if(bytes < 1024) {
            return bytes + " B";
        }

        String[] units = new String[]{"KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));

        // make sure the file is not bigger than YB
        if(unitIndex > units.length) {
            return bytes + " B";
        }

        String unit = units[unitIndex - 1];

        double size = bytes / Math.pow(1024, unitIndex);

        String readableSize = String.format("%3.1f", size);

        // 233.55
        if(readableSize.length() == 5) {
            readableSize = readableSize.substring(0, 3);
        }
        else if(readableSize.length() == 3) { // 3.8
            readableSize = String.format("%3.2f", size);
        }
        else { // 20.29
            readableSize = String.format("%3.1f", size);
        }

        return readableSize + " " + unit;
    }

    /**
     * Gets a human readable time in the following format:
     * #h #m #s #ms
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return a human readable time
     */
    public static String getReadableTime(long startTime, long endTime) {
        long elapsedTime = endTime - startTime;

        long hours = TimeUnit.NANOSECONDS.toHours(elapsedTime);
        elapsedTime -= TimeUnit.HOURS.toNanos(hours);

        long minutes = TimeUnit.NANOSECONDS.toMinutes(elapsedTime);
        elapsedTime -= TimeUnit.MINUTES.toNanos(minutes);

        long seconds = TimeUnit.NANOSECONDS.toSeconds(elapsedTime);
        elapsedTime -= TimeUnit.SECONDS.toNanos(seconds);

        long millis = TimeUnit.NANOSECONDS.toMillis(elapsedTime);

        return String.format("%dh %dm %ds %dms", hours, minutes, seconds, millis);
    }

    /**
     * Converts a string to a list with its elements separated by a new line separator.
     *
     * @param str the string
     * @return the list or null
     */
    public static List<String> getStringAsList(String str) {
        return getStringAsList(str, SEPARATOR);
    }

    /**
     * Converts a string to a list with its elements separated by a separator.
     *
     * @param str       the string
     * @param separator the separator
     * @return the list or null
     */
    public static List<String> getStringAsList(String str, String separator) {
        if(str == null) {
            return null;
        }

        List<String> list = new ArrayList<>();
        String[] strArr = str.split(Pattern.quote(separator));
        Collections.addAll(list, strArr);

        return list;
    }

    /**
     * Gets data from a filename line by line into a list.
     *
     * @param filename the filename
     * @return the list, otherwise null
     */
    public static List<String> loadAsList(String filename) {
        String data = loadAsString(filename);
        if(data == null) {
            return null;
        }

        return Arrays.asList(data.split(Pattern.quote(SEPARATOR)));
    }

    /**
     * Gets data from a filename.
     *
     * @param filename the filename
     * @return the contents of the file as a string, otherwise null
     */
    public static String loadAsString(String filename) {
        if(filename == null) {
            return null;
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while(line != null) {
                sb.append(line);
                sb.append(SEPARATOR);

                line = br.readLine();
            }

            // remove last separator
            String data = sb.toString();
            int lastSeparatorIndex = data.lastIndexOf(SEPARATOR);
            if(lastSeparatorIndex != -1) {
                data = data.substring(0, lastSeparatorIndex);
            }

            return data;
        }
        catch(FileNotFoundException e) {
        }
        catch(IOException e) {
        }

        return null;
    }

    /**
     * Attempts to open a given file in the user's default file manager.
     *
     * @param filename the filename of the file
     * @return true on success, otherwise false
     */
    public static boolean openFile(String filename) {
        return openFile(new File(filename));
    }

    /**
     * Attempts to open a given file in the user's default file manager.
     *
     * @param file the file
     * @return true on success, otherwise false
     */
    public static boolean openFile(File file) {
        if(file == null) {
            return false;
        }

        try {
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);

                return true;
            }
        }
        catch(IOException e) {
        }

        return false;
    }

    /**
     * Saves data to a filename.
     *
     * @param filename the filename
     * @param data     the data to write
     * @return true on success
     */
    public static boolean save(String filename, String data) {
        if(filename == null || data == null) {
            return false;
        }

        try(FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            byte[] bytes = data.getBytes("UTF8");

            fileOutputStream.write(bytes);
            fileOutputStream.flush();

            return true;
        }
        catch(IOException e) {
        }

        return false;
    }

    /**
     * Saves data to a filename if the data is different from the one already in the file.
     *
     * @param filename the filename
     * @param data     the data to write
     * @return true on success
     */
    public static boolean saveIfDifferent(String filename, String data) {
        if(filename == null || data == null) {
            return false;
        }

        // if the data is the same as the one in the file we are done
        if(data.equals(loadAsString(filename))) {
            return true;
        }

        return save(filename, data);
    }

    /**
     * Serializes an object to a file.
     *
     * @param filename the filename
     * @param object   the object
     * @return true on success
     */
    public static boolean serialize(String filename, Object object) {
        if(filename == null || object == null) {
            return false;
        }

        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
            objectOutputStream.writeObject(object);

            return true;
        }
        catch(FileNotFoundException e) {
        }
        catch(IOException e) {
        }

        return false;
    }

    /**
     * Returns a string trimmed from both ends.
     *
     * @param source the source string
     * @param target the string to be trimmed off from both sides of the source string
     * @return the trimmed source string or null
     */
    public static String trim(String source, String target) {
        source = trimFromTheFront(source, target);
        source = trimFromTheBack(source, target);

        return source;
    }

    /**
     * Returns a string trimmed from the back.
     *
     * @param source the source string
     * @param target the string to be trimmed off from both sides of the source string
     * @return the trimmed source string or null
     */
    public static String trimFromTheBack(String source, String target) {
        if(source == null || target == null) {
            return null;
        }

        while(source.endsWith(target)) {
            source = source.substring(0, source.length() - target.length());
        }

        return source;
    }

    /**
     * Returns a string trimmed from the front.
     *
     * @param source the source string
     * @param target the string to be trimmed off from both sides of the source string
     * @return the trimmed source string or null
     */
    public static String trimFromTheFront(String source, String target) {
        if(source == null || target == null) {
            return null;
        }

        while(source.startsWith(target)) { // trim the front of the source string
            source = source.substring(target.length());
        }

        return source;
    }
}