package zipkin2.internal;

public interface GlobalExtrasSupplier {
    String service();

    String system();
}
