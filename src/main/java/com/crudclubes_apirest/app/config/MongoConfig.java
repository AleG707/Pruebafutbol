package com.crudclubes_apirest.app.config;

import com.crudclubes_apirest.app.entidades.asociacion;
import com.crudclubes_apirest.app.entidades.competicion;
import com.crudclubes_apirest.app.entidades.entrenador;
import com.crudclubes_apirest.app.entidades.jugador;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuración explícita de conexión a MongoDB Cloud Atlas.
 * Garantiza la conectividad con el clúster en la nube, timeouts y la creación del cliente Mongo y MongoTemplate.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.crudclubes_apirest.app.repositorios")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb+srv://Gabriel_DB:UTS2026@cluster0.wws2n1k.mongodb.net/DB?retryWrites=true&w=majority}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:DB}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder -> 
                        builder.connectTimeout(15, TimeUnit.SECONDS)
                               .readTimeout(15, TimeUnit.SECONDS))
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(15, TimeUnit.SECONDS))
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    /**
     * Conversores personalizados de lectura para dar tolerancia a documentos antiguos
     * o datos migrados donde entrenador, asociación, jugador o competición fueron almacenados
     * como IDs numéricos en lugar de subdocumentos embebidos.
     */
    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new LongToEntrenadorConverter(),
                new IntegerToEntrenadorConverter(),
                new StringToEntrenadorConverter(),
                new LongToAsociacionConverter(),
                new IntegerToAsociacionConverter(),
                new StringToAsociacionConverter(),
                new LongToJugadorConverter(),
                new IntegerToJugadorConverter(),
                new LongToCompeticionConverter(),
                new IntegerToCompeticionConverter()
        ));
    }

    @ReadingConverter
    public static class LongToEntrenadorConverter implements Converter<Long, entrenador> {
        @Override
        public entrenador convert(Long source) {
            return new entrenador("Entrenador #" + source, "(ID legacy)", 40, "No especificada");
        }
    }

    @ReadingConverter
    public static class IntegerToEntrenadorConverter implements Converter<Integer, entrenador> {
        @Override
        public entrenador convert(Integer source) {
            return new entrenador("Entrenador #" + source, "(ID legacy)", 40, "No especificada");
        }
    }

    @ReadingConverter
    public static class StringToEntrenadorConverter implements Converter<String, entrenador> {
        @Override
        public entrenador convert(String source) {
            return new entrenador(source, "", 40, "No especificada");
        }
    }

    @ReadingConverter
    public static class LongToAsociacionConverter implements Converter<Long, asociacion> {
        @Override
        public asociacion convert(Long source) {
            return new asociacion("Asociación #" + source, "N/A", "N/A");
        }
    }

    @ReadingConverter
    public static class IntegerToAsociacionConverter implements Converter<Integer, asociacion> {
        @Override
        public asociacion convert(Integer source) {
            return new asociacion("Asociación #" + source, "N/A", "N/A");
        }
    }

    @ReadingConverter
    public static class StringToAsociacionConverter implements Converter<String, asociacion> {
        @Override
        public asociacion convert(String source) {
            return new asociacion(source, "N/A", "N/A");
        }
    }

    @ReadingConverter
    public static class LongToJugadorConverter implements Converter<Long, jugador> {
        @Override
        public jugador convert(Long source) {
            return new jugador(source, "Jugador #" + source, "", 0, "No especificada");
        }
    }

    @ReadingConverter
    public static class IntegerToJugadorConverter implements Converter<Integer, jugador> {
        @Override
        public jugador convert(Integer source) {
            return new jugador(source.longValue(), "Jugador #" + source, "", 0, "No especificada");
        }
    }

    @ReadingConverter
    public static class LongToCompeticionConverter implements Converter<Long, competicion> {
        @Override
        public competicion convert(Long source) {
            return new competicion(source, "Competición #" + source, 0, null, null);
        }
    }

    @ReadingConverter
    public static class IntegerToCompeticionConverter implements Converter<Integer, competicion> {
        @Override
        public competicion convert(Integer source) {
            return new competicion(source.longValue(), "Competición #" + source, 0, null, null);
        }
    }
}

