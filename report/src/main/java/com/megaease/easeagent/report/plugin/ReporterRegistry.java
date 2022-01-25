package com.megaease.easeagent.report.plugin;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.report.sender.NoOpSender;
import com.megaease.easeagent.report.sender.SenderConfigDecorator;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ReporterRegistry {
    static Logger logger = LoggerFactory.getLogger(ReporterRegistry.class);

    static ConcurrentHashMap<String, Supplier<Encoder<?>>> encoders = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Supplier<Sender>> senderSuppliers = new ConcurrentHashMap<>();

    private ReporterRegistry() {}

    public static void registryEncoder(String name, Supplier<Encoder<?>> encoder) {
        Supplier<Encoder<?>> o = encoders.putIfAbsent(name, encoder);
        if (o != null) {
            String on = o.get().getClass().getSimpleName();
            String cn = encoder.get().getClass().getSimpleName();
            logger.error("Encoder name conflict:{}, between {} and {}", name, on, cn);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> Encoder<V> getEncoder(String name) {
        if (encoders.get(name) == null) {
            logger.error("Encoder name \"{}\" is not exists!", name);
            return (Encoder<V>) NoOpEncoder.INSTANCE;
        }
        Encoder<V> encoder = (Encoder<V>)encoders.get(name).get();

        if (encoder == null) {
            return (Encoder<V>)NoOpEncoder.INSTANCE;
        }

        return encoder;
    }

    public static void registrySender(String name, Supplier<Sender> sender) {
        Supplier<Sender> o = senderSuppliers.putIfAbsent(name, sender);
        if (o != null) {
            String on = o.get().getClass().getSimpleName();
            String cn = sender.get().getClass().getSimpleName();
            logger.error("Sender name conflict:{}, between {} and {}", name, on, cn);
        }
    }

    public static SenderWithEncoder getSender(String prefix, Config config) {
        String name = config.getString(prefix + ".name");
        SenderWithEncoder sender = new SenderConfigDecorator(prefix, getSender(name), config);
        sender.init(config);
        return sender;
    }

    public static Sender getSender(String name) {
        Supplier<Sender> supplier = senderSuppliers.get(name);
        if (supplier == null) {
            return NoOpSender.INSTANCE;
        }
        return supplier.get();
    }
}
