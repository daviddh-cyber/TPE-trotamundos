package com.trotamundos.viajes.config;

import com.trotamundos.viajes.eventos.Topicos;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Crea los tópicos que usa el servicio si todavía no existen. */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicoReservasRegistradas() {
        return TopicBuilder.name(Topicos.RESERVAS_REGISTRADAS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicoViajesActualizados() {
        return TopicBuilder.name(Topicos.VIAJES_ACTUALIZADOS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicoCambiosReserva() {
        return TopicBuilder.name(Topicos.CAMBIOS_RESERVA).partitions(3).replicas(1).build();
    }
}
