package com.compileordie.pvz2.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class KryoSerializer {

    private KryoSerializer() {}

    private static final Pool<Kryo> KRYO_POOL = new Pool<Kryo>(true, false, 8) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);             // Handle looped / circular references
            kryo.setRegistrationRequired(false);  // Allow custom game classes without explicit ID registration
            // Set strategy directly without the DefaultInstantiatorStrategy wrapper
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    public static String serialize(Object object) {
        if (object == null) return "";
        Kryo kryo = KRYO_POOL.obtain();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Output output = new Output(baos)) {
            kryo.writeClassAndObject(output, object);
            output.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } finally {
            KRYO_POOL.free(kryo);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String base64Data, Class<T> clazz) {
        if (base64Data == null || base64Data.isEmpty()) return null;
        Kryo kryo = KRYO_POOL.obtain();
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            Object obj = kryo.readClassAndObject(input);
            return (T) obj;
        } finally {
            KRYO_POOL.free(kryo);
        }
    }
}
