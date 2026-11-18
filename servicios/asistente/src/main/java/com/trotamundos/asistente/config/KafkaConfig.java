package com.trotamundos.asistente.config;

import com.trotamundos.asistente.eventos.Topicos;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Crea los tópicos que usa el servicio si todavía no existen. */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicoCasosDerivados() {
        return TopicBuilder.name(Topicos.CASOS_DERIVADOS).partitions(3).replicas(1).build();
    }
}
