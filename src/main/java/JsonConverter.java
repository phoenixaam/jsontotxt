import java.nio.file.Path;

public interface JsonConverter {

    String jsonToTxt(Path path) throws JsonConverterException;

    String txtToJson(Path path) throws JsonConverterException;

}
