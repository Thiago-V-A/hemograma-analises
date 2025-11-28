CREATE TABLE hemograms_partitioned (
                                       LIKE hemograms INCLUDING ALL
) PARTITION BY RANGE (received_at);

-- Criar partições para os próximos 6 meses
CREATE TABLE hemograms_2025_01 PARTITION OF hemograms_partitioned
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE hemograms_2025_02 PARTITION OF hemograms_partitioned
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

CREATE TABLE hemograms_2025_03 PARTITION OF hemograms_partitioned
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');

CREATE TABLE hemograms_2025_04 PARTITION OF hemograms_partitioned
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');

CREATE TABLE hemograms_2025_05 PARTITION OF hemograms_partitioned
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');

CREATE TABLE hemograms_2025_06 PARTITION OF hemograms_partitioned
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');