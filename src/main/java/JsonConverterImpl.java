import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class JsonConverterImpl implements JsonConverter {

    private static final int MAXTHREADNUMBER = getProcessorsCount() * 4;

    private enum ConverterType {
        TOTXT, TOJSON
    }


    private static int getProcessorsCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public String jsonToTxt(Path path) throws JsonConverterException {
        Objects.requireNonNull(path);
        String result;
        try {
            List<String> lines = Files.readAllLines(path);
            result = getConvertedString(lines, ConverterType.TOTXT);
        } catch (IOException e) {
            throw new JsonConverterException("Exception while reading json file.");
        }

        return result;
    }

    public String txtToJson(final Path path) throws JsonConverterException {
        Objects.requireNonNull(path);
        String result;
        try {
            List<String> lines = Files.readAllLines(path);
            result = getConvertedString(lines, ConverterType.TOJSON);
        } catch (IOException e) {
            throw new JsonConverterException("Exception while reading txt file.");
        }
        return result;
    }

    private String getConvertedString(final List<String> source, ConverterType type) throws JsonConverterException {
        List<FutureTask> tasks = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executorService = Executors.newFixedThreadPool(MAXTHREADNUMBER);
        for (String s : source) {
            Callable<String> stringHandler = null;
            if (type == ConverterType.TOJSON) {
                stringHandler = new StringTxtToJsonHandler(s);
            } else if (type == ConverterType.TOTXT) {
                stringHandler = new StringJsonToTxtHandler(s);
            }
            FutureTask<String> task = new FutureTask<>(stringHandler);
            tasks.add(task);
            executorService.execute(task);
        }
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new JsonConverterException("Exception while closing thread pool");
        }
        executorService.shutdownNow();

        StringBuilder stringBuilder = new StringBuilder("");
        if (type == ConverterType.TOJSON) {
            stringBuilder.append("{\n");
        }
        for (FutureTask futureTask : tasks) {
            try {
                stringBuilder.append(futureTask.get());
                stringBuilder.append("\n");
            } catch (ExecutionException | InterruptedException e) {
                throw new JsonConverterException("Exception while reading future.");
            }
        }
        if (type == ConverterType.TOJSON) {
            stringBuilder.append("}");
            ;
        }

        return stringBuilder.toString();
    }

    public static void main(String[] args) throws JsonConverterException {
        System.out.println(new JsonConverterImpl().txtToJson(Paths.get("C:\\temp\\toJson.txt")));
        System.out.println(new JsonConverterImpl().jsonToTxt(Paths.get("C:\\temp\\toTxt.json")));
    }
}

class StringTxtToJsonHandler implements Callable<String> {

    private final String s;

    public StringTxtToJsonHandler(String s) {
        this.s = s;
    }

    @Override
    public String call() throws Exception {
        String[] strArr = s.split("->");
        if (strArr.length == 2) {
            return "\"" + strArr[0] + "\":\"" + strArr[1] + "\"";
        } else {
            throw new JsonConverterException("Incorrect txt file structure.");
        }
    }
}

class StringJsonToTxtHandler implements Callable<String> {

    private final String s;

    public StringJsonToTxtHandler(String s) {
        this.s = s;
    }

    @Override
    public String call() throws Exception {
        if (s.equals("{") || s.equals("}")) {
            return "";
        } else {
            String[] strArr = s.split("\":\"");
            if (strArr.length == 2) {
                return strArr[0].substring(1) + "->" + strArr[1].substring(0, strArr[1].length() - 1);
            } else {
                throw new JsonConverterException("Incorrect json file structure.");
            }
        }
    }
}
