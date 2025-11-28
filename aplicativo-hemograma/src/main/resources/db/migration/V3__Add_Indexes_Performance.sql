CREATE INDEX idx_hemograms_has_alert ON hemograms(has_individual_alert)
    WHERE has_individual_alert = true;

CREATE INDEX idx_hemograms_region_date_alert ON hemograms(patient_region, received_at, has_individual_alert);

CREATE INDEX idx_region_stats_calculated ON region_stats(calculated_at DESC);

CREATE INDEX idx_alerts_unacknowledged ON collective_alerts(acknowledged, severity, created_at)
    WHERE acknowledged = false;

CREATE INDEX idx_notifications_user_unread ON alert_notifications(user_id, is_read)
    WHERE is_read = false;