package com.crudclubes_apirest.app.servicios;

import com.crudclubes_apirest.app.entidades.DatabaseSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Servicio encargado de generar identificadores numéricos incrementales y atómicos en MongoDB.
 * Utiliza la colección auxiliar 'database_sequences' y operaciones findAndModify atómicas.
 */
@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    @Autowired
    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    /**
     * Incrementa de forma atómica y retorna el siguiente valor de secuencia para una clave dada.
     * @param seqName nombre de la secuencia (e.g. club.SEQUENCE_NAME, jugador.SEQUENCE_NAME, competicion.SEQUENCE_NAME)
     * @return siguiente valor secuencial (Long)
     */
    public long generateSequence(String seqName) {
        Query query = new Query(Criteria.where("_id").is(seqName));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);

        DatabaseSequence counter = mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);
        return !Objects.isNull(counter) ? counter.getSeq() : 1L;
    }
}
