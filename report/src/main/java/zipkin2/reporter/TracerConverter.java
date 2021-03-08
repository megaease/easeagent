package zipkin2.reporter;

import java.util.List;

public interface TracerConverter {

    List<byte[]> converter(List<byte[]> nextMessage);


}
