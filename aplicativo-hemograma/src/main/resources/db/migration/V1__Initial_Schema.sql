CREATE TABLE hemograms (
                           id BIGSERIAL PRIMARY KEY,
                           fhir_id VARCHAR(100) NOT NULL UNIQUE,
                           patient_region VARCHAR(50) NOT NULL,
                           health_unit VARCHAR(100),
                           received_at TIMESTAMP NOT NULL,
                           observation_date TIMESTAMP,

    -- Parâmetros hematológicos
                           leukocytes DOUBLE PRECISION,
                           platelets DOUBLE PRECISION,
                           hemoglobin DOUBLE PRECISION,
                           hematocrit DOUBLE PRECISION,
                           neutrophils DOUBLE PRECISION,
                           lymphocytes DOUBLE PRECISION,
                           monocytes DOUBLE PRECISION,
                           eosinophils DOUBLE PRECISION,
                           basophils DOUBLE PRECISION,

    -- Alertas individuais
                           has_individual_alert BOOLEAN DEFAULT FALSE,
                           alert_parameters VARCHAR(500),

    -- FHIR raw
                           fhir_raw TEXT,
                           processed_at TIMESTAMP,

                           version BIGINT DEFAULT 0,

                           CONSTRAINT chk_leukocytes CHECK (leukocytes IS NULL OR leukocytes >= 0),
                           CONSTRAINT chk_platelets CHECK (platelets IS NULL OR platelets >= 0),
                           CONSTRAINT chk_hemoglobin CHECK (hemoglobin IS NULL OR hemoglobin >= 0)
);

CREATE INDEX idx_region_received ON hemograms(patient_region, received_at);
CREATE INDEX idx_fhir_id ON hemograms(fhir_id);
CREATE INDEX idx_received_at ON hemograms(received_at);

-- Tabela de estatísticas regionais
CREATE TABLE region_stats (
                              id BIGSERIAL PRIMARY KEY,
                              region VARCHAR(50) NOT NULL,
                              window_start TIMESTAMP NOT NULL,
                              window_end TIMESTAMP NOT NULL,

                              total_hemograms INTEGER NOT NULL,
                              alerted_hemograms INTEGER,
                              alert_proportion DOUBLE PRECISION,

    -- Estatísticas de leucócitos
                              avg_leukocytes DOUBLE PRECISION,
                              std_leukocytes DOUBLE PRECISION,
                              leukocytes_trend DOUBLE PRECISION,

    -- Estatísticas de plaquetas
                              avg_platelets DOUBLE PRECISION,
                              std_platelets DOUBLE PRECISION,
                              platelets_trend DOUBLE PRECISION,

    -- Estatísticas de hemoglobina
                              avg_hemoglobin DOUBLE PRECISION,
                              std_hemoglobin DOUBLE PRECISION,
                              hemoglobin_trend DOUBLE PRECISION,

                              calculated_at TIMESTAMP NOT NULL,
                              previous_stats_id BIGINT,

                              version BIGINT DEFAULT 0,

                              FOREIGN KEY (previous_stats_id) REFERENCES region_stats(id) ON DELETE SET NULL
);

CREATE INDEX idx_region_window ON region_stats(region, window_start, window_end);
CREATE INDEX idx_window_end ON region_stats(window_end);

-- Tabela de alertas coletivos
CREATE TABLE collective_alerts (
                                   id BIGSERIAL PRIMARY KEY,
                                   alert_code VARCHAR(50) NOT NULL UNIQUE,
                                   region VARCHAR(50) NOT NULL,
                                   parameter VARCHAR(50) NOT NULL,
                                   severity VARCHAR(20) NOT NULL,

                                   affected_hemograms INTEGER NOT NULL,
                                   total_hemograms INTEGER NOT NULL,
                                   alert_proportion DOUBLE PRECISION,

    -- Valores estatísticos
                                   current_avg DOUBLE PRECISION,
                                   previous_avg DOUBLE PRECISION,
                                   trend_percent DOUBLE PRECISION,
                                   std_deviation DOUBLE PRECISION,

                                   window_start TIMESTAMP,
                                   window_end TIMESTAMP,

                                   description TEXT,
                                   recommended_action TEXT,

                                   fhir_communication_id VARCHAR(100),

                                   created_at TIMESTAMP NOT NULL,
                                   acknowledged BOOLEAN DEFAULT FALSE,
                                   acknowledged_at TIMESTAMP,
                                   acknowledged_by VARCHAR(100),
                                   notes TEXT,

                                   region_stats_id BIGINT,

                                   version BIGINT DEFAULT 0,

                                   FOREIGN KEY (region_stats_id) REFERENCES region_stats(id) ON DELETE SET NULL,

                                   CONSTRAINT chk_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_region_created ON collective_alerts(region, created_at);
CREATE INDEX idx_severity ON collective_alerts(severity);
CREATE INDEX idx_acknowledged ON collective_alerts(acknowledged);

-- Tabela de notificações
CREATE TABLE alert_notifications (
                                     id BIGSERIAL PRIMARY KEY,
                                     alert_id BIGINT NOT NULL,
                                     user_id VARCHAR(100) NOT NULL,
                                     device_token VARCHAR(500) NOT NULL,

                                     notification_title VARCHAR(200),
                                     notification_body TEXT,

                                     sent_at TIMESTAMP NOT NULL,
                                     is_read BOOLEAN DEFAULT FALSE,
                                     read_at TIMESTAMP,

                                     fcm_message_id VARCHAR(200),
                                     status VARCHAR(20) NOT NULL,
                                     error_message VARCHAR(500),

                                     version BIGINT DEFAULT 0,

                                     FOREIGN KEY (alert_id) REFERENCES collective_alerts(id) ON DELETE CASCADE,

                                     CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DELIVERED'))
);

CREATE INDEX idx_alert_user ON alert_notifications(alert_id, user_id);
CREATE INDEX idx_sent_at ON alert_notifications(sent_at);
CREATE INDEX idx_read ON alert_notifications(is_read);
